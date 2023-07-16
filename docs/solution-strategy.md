# Solution strategy

I have decided to use Apache Lucene because it is part of the Elastic Search core,
and it is my understanding that ES is part of the tools that I should use on  the job.
I worked with Elastic Search in the past, and even though I thought at first I might
spawn a docker-container with docker-compose, I also considered the fact that 
you may not have docker installed and/or your company policies might not allow you
to do so.
Hence, as a simulation, I used the in memory version of Apache Lucene which comes
with some limitations (some not so surprisingly are present also in ES as well).
Lucene has a lock on the writer, and it does not have something like a primary key that enforces
to have unique documents. The developer has to do this kind of validation.

In order to avoid duplicates you should use the following code

```java
package com.foobar.foobarchallenge.port.adapter.persistence;

import com.foobar.foobarchallenge.domain.model.AlternativeName;
import com.foobar.foobarchallenge.domain.model.DocumentSchema;
import com.foobar.foobarchallenge.domain.model.Name;
import com.foobar.foobarchallenge.domain.repo.CompanyRepository;
import com.foobar.foobarchallenge.domain.repo.DocumentRepository;
import com.foobar.foobarchallenge.port.adapter.mapper.DocumentMapper;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LocalDocumentRepository implements DocumentRepository {
    private final Try.WithResources1<IndexWriter> indexWriterTry;
    private final BiFunction<String[], Analyzer, QueryParser> queryParserFactory;
    private final Directory memoryIndex;
    private final DocumentMapper mapper;
    private final CompanyRepository companyRepository;
    private final Analyzer analyzer;
    private final ReentrantLock lock = new ReentrantLock(true);

    @Override
    @Retryable(
            maxAttempts = 50,
            backoff = @Backoff(random = true, delay = 10, maxDelay = 300, multiplier = 2))
    public long index(List<DocumentSchema> schemas) {

        //    Try.WithResources1<IndexWriter> indexWriterTry = indexWriterSupplier.get();

        return indexWriterTry
                .of(
                        writer -> {
                            var mappedDocuments = filterDocuments(schemas);
                            return addDocuments(writer, mappedDocuments);
                        })
                .onFailure(
                        error ->
                                log.error(
                                        "LockObtainFailedException in LocalDocumentRepository:indexWriterConfig with message: {}, {}",
                                        error.getMessage(),
                                        List.of(error.getStackTrace())))
                .getOrElseThrow(error -> new RuntimeException(error));
    }

    private Long addDocuments(IndexWriter writer, List<Document> mappedDocuments) {
        return Try.of(() -> {

                    /****************************************
                     *                                      *    
                     ****** this is the important part ****** 
                     *                                      * 
                     ****************************************/
                    lock.lock();
                    try {
                        for (Document doc : mappedDocuments) {
                            String id = doc.get("id");
                            // retrieve the document by id, if found don't add
                            Query query = new TermQuery(new Term("id", id));
                            IndexReader reader = DirectoryReader.open(writer);
                            IndexSearcher searcher = new IndexSearcher(reader);
                            TopDocs search = searcher.search(query, 1);
                            if (search.scoreDocs.length == 0) {
                                // If document is not found, then add it
                                writer.addDocument(doc);
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                    return 0L;
                })
                .onFailure(
                        error ->
                                log.error(
                                        "LockObtainFailedException in LocalDocumentRepository:indexWriterConfig with message: {}, {}",
                                        error.getMessage(),
                                        List.of(error.getStackTrace())))
                .getOrElse(-1L);
    }

    private List<Document> filterDocuments(List<DocumentSchema> schemas) {

        return schemas.stream()
                .filter(LocalDocumentRepository::isValidDocumentSchema)
                .map(mapper::mapToLuceneDocument)
                .toList();
    }

    private static boolean isValidDocumentSchema(DocumentSchema x) {
        return Validation.combine(
                        validateNonNullAndNotEmpty(x.title(), "text"),
                        validateNonNullAndNotEmpty(x.text(), "title"),
                        validateNonNullAndNotEmpty(x.id(), "id"))
                .ap((title, text, id) -> x)
                .isValid();
    }

    private static Validation<String, String> validateNonNullAndNotEmpty(
            String value, String fieldName) {
        if (value != null && !value.isEmpty()) {
            return Validation.valid(value);
        } else {
            return Validation.invalid(String.format("%s should not be null or empty", fieldName));
        }
    }

    @Override
    public List<Document> getDocumentsBySearchPhrase(Query query) {
        Either<Throwable, List<Document>> documentsEither =
                Try.of(() -> handleGetDocuments(query)).toEither();
        return documentsEither.fold(
                error -> {
                    log.error(
                            "Exception in LocalDocumentRepository:getDocumentsBySearchPhrase with message: {}, {}",
                            error.getMessage(),
                            List.of(error.getStackTrace()));
                    return Collections.emptyList();
                },
                documents -> documents);
    }

    private List<Document> handleGetDocuments(Query query) throws IOException {
        try (IndexReader indexReader = DirectoryReader.open(memoryIndex)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            TopDocs topDocs = searcher.search(query, indexReader.numDocs());
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc));
            }
            return documents;
        }
    }

    @Override
    public long countAllDocuments() {
        try (IndexReader reader = DirectoryReader.open(memoryIndex)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = createMatchAllDocsQuery();
            TotalHits totalHits = searcher.search(query, reader.numDocs()).totalHits;
            long count = totalHits.value;
            log.info("Total number of successfully indexed documents: {}", count);
            return count;
        } catch (IllegalArgumentException | IOException e) {
            log.error(
                    "Exception in LocalDocumentRepository:countDocuments with message: {}, {}",
                    e.getMessage(),
                    List.of(e.getStackTrace()));
        }
        return -1L;
    }

    @Override
    @SneakyThrows
    public List<Document> getDocumentsWithCompanyNameInTextAndTitle(String companyName) {
        List<String> alternatives = getAlternatives(companyName);
        String[] fields = {"text", "title"};
        var searchValues = getAggregatedCompanyNames(companyName, alternatives);

        return getDocumentsBySearchPhrase(
                queryParserFactory.apply(fields, analyzer).parse(searchValues));
    }

    private String getAggregatedCompanyNames(String companyName, List<String> alternatives) {
        return Stream.of(List.of(companyName), alternatives)
                .flatMap(Collection::stream)
                .map(x -> String.format("\"%s\"", x))
                .collect(Collectors.joining(" OR "));
    }

    private List<String> getAlternatives(String companyName) {
        return companyRepository.getCompanyAlternativeNames(new Name(companyName)).stream()
                .map(AlternativeName::toString)
                .toList();
    }

    @Override
    public List<String> getDocumentTitlesWithCompanyNameInText(String companyName) {
        return getDocumentsWithCompanyNameInTextAndTitle(companyName).stream()
                .map(x -> x.get("title"))
                .toList();
    }

    @Override
    public List<String> getDocumentIdsWithCompanyNameInText(String companyName) {
        return getDocumentsWithCompanyNameInTextAndTitle(companyName).stream()
                .map(x -> x.get("id"))
                .toList();
    }

    private static MatchAllDocsQuery createMatchAllDocsQuery() {
        return new MatchAllDocsQuery();
    }
}
```
However, by doing so, you have to swallow a huge performance hit: from 6 seconds to 30.9 seconds.

This problem with duplicates seems to be present also on Elastic Search and there are some techniques to delete the duplicates.
So, in cases where performance is more important, you can probably have a scheduled job that filters the duplicates to keep
the data set as clean as possible. 
An additional measure would be to have a separate service (or another endpoint in this service) which filters the documents 
from duplicates before they are sent to have a clean dataset.
This depends also on the cloud provider subscriptions and cost associated. 
Instances with more cores and RAM are more costly. Having a separate service with less CPU cores and RAM might be cheaper.

Otherwise, another approach could be to choose another NoSQL database which support the enforcement of unique entries based on a key
for example MongoDB or CouchDB.

Just for the sake of this exercise, I have used some interfaces like: 
- ApplicationDocumentService
- CompanyRepository
- DocumentRepository
Usually I prefer to avoid using interfaces from start, and I use them just when I really need them.
Thinking of hypothetical use cases which do not exist yet bring usually to an additional complexity that oftentimes
it is not justified.
Premature optimization and premature abstractions bring unneeded complexity.

Since this is a CRUD with not much business logic, we have kind of an anemic model.

Functional programming helps us to load in memory the information only when it is necessary
(lazy initialization) and, with immutability, we can achieve trade safety and easier debugging.

I prefer composition to inheritance, using an inversion of control container, because inheritance couples
your code to a base class, with the disadvantage that you can break the invariants. 
Also testing is more complicated and might require Powermock. 
Therefore, I just use Dependency Injection and, when needed, interfaces, along with design patterns such as lightweight strategy
pattern with enums, builder, factory, chain of responsibility.


