package com.foobar.foobarchallenge.port.adapter.persistence;

import com.foobar.foobarchallenge.common.Repository;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

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

  @Override
  @Retryable(
      maxAttempts = 50,
      backoff = @Backoff(random = true, delay = 10, maxDelay = 300, multiplier = 2))
  public long index(List<DocumentSchema> schemas) {

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

  private static Long addDocuments(IndexWriter writer, List<Document> mappedDocuments) {
    // addDocuments under the hood calls updateDocuments
    return Try.of(() -> writer.addDocuments(mappedDocuments))
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
