package com.foobar.foobarchallenge.application.service;

import static com.foobar.foobarchallenge.domain.FoobarException.ErrorCode.CANNOT_LOAD_ARTICLE_FILE;

import com.foobar.foobarchallenge.common.ApplicationService;
import com.foobar.foobarchallenge.domain.FoobarException;
import com.foobar.foobarchallenge.domain.model.DocumentSchema;
import com.foobar.foobarchallenge.domain.model.ID;
import com.foobar.foobarchallenge.domain.repo.CompanyRepository;
import com.foobar.foobarchallenge.domain.repo.DocumentRepository;
import com.foobar.foobarchallenge.port.CustomResourceLoader;
import com.foobar.foobarchallenge.port.adapter.mapper.DocumentMapper;
import com.foobar.foobarchallenge.resource.out.OutDocumentDto;
import com.foobar.foobarchallenge.spring.property.ApplicationDocumentServiceProperties;
import io.micrometer.core.annotation.Timed;
import io.vavr.control.Try;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;

@ApplicationService
@RequiredArgsConstructor
@Slf4j
public class ApplicationDocumentServiceImpl implements ApplicationDocumentService {

  public static final String ARTICLES_PATH = "/inputdata/articles";

  @Qualifier("documentServiceExecutor")
  private final Executor executor;

  private final DocumentRepository localDocumentRepository;
  private final DocumentMapper documentMapper;
  private final ApplicationDocumentServiceProperties applicationDocumentServiceProperties;
  private final CustomResourceLoader customResourceLoader;
  private final CompanyRepository companyRepository;

  @Async
  @Timed(value = "document.addDocuments", description = "Adding documents from the controller")
  public void addDocuments(List<DocumentSchema> articles) {
    handleIndexing(
        articles.stream()
            .map(Optional::ofNullable)
            .filter(Optional::isPresent)
            .map(article -> CompletableFuture.supplyAsync(() -> article))
            .toList());
  }

  @Override
  @Timed(value = "documents.index", description = "Indexing documents on application startups")
  public void index() {
    List<File> resources =
        customResourceLoader.getResources(ApplicationDocumentServiceImpl.ARTICLES_PATH).stream()
            .filter(filterOnlyXmlFiles())
            .map(this::getFile)
            .toList();

    Function<List<File>, List<CompletableFuture<Optional<DocumentSchema>>>> schemasFutures =
        r -> r.stream().map(this::processInputFile).toList();

    handleIndexing(schemasFutures.apply(resources));
  }

  private void handleIndexing(List<CompletableFuture<Optional<DocumentSchema>>> schemasFutures) {
    int batchSize = applicationDocumentServiceProperties.batchSize();
    int totalBatches = (int) Math.ceil((double) schemasFutures.size() / batchSize);
    for (int batch = 0; batch < totalBatches; batch++) {
      int fromIndex = batch * batchSize;
      int toIndex = Math.min(fromIndex + batchSize, schemasFutures.size());

      List<DocumentSchema> parsedDocuments =
          schemasFutures.subList(fromIndex, toIndex).stream()
              .map(f -> f.thenApplyAsync(x -> x, executor).orTimeout(1, TimeUnit.SECONDS).join())
              .filter(Optional::isPresent)
              .map(Optional::get)
              .toList();

      localDocumentRepository.index(parsedDocuments);
      log.info("Batch Document parsing operation #{} of #{} completed", batch + 1, totalBatches);
    }
  }

  private File getFile(Resource x) {
    return Try.of(x::getFile)
        .getOrElseThrow(e -> new FoobarException(CANNOT_LOAD_ARTICLE_FILE, e.getMessage()));
  }

  private CompletableFuture<Optional<DocumentSchema>> processInputFile(File r) {
    return CompletableFuture.supplyAsync(() -> documentMapper.parse(r), executor);
  }

  private Predicate<Resource> filterOnlyXmlFiles() {
    return r -> Objects.requireNonNull(r.getFilename()).endsWith(".xml");
  }

  @Override
  public OutDocumentDto getDocumentsByCompanyIds(List<String> ids) {
    var companyNames =
        ids.stream()
            .map(id -> Optional.ofNullable(companyRepository.getCompanyNameById(new ID(id))))
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .toList();

    var data =
        companyNames.stream()
            .map(
                name ->
                    localDocumentRepository.getDocumentsWithCompanyNameInTextAndTitle(
                        name.toString()))
            .map(docs -> docs.stream().map(documentMapper::mapDocumentToDocumentSchema).toList())
            .flatMap(Collection::stream)
            .toList();
    return new OutDocumentDto(data);
  }
}
