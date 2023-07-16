package com.foobar.foobarchallenge.spring.configuration;

import com.foobar.foobarchallenge.domain.repo.DocumentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomDocumentMetrics implements InitializingBean {

  private final MeterRegistry meterRegistry;
  private final DocumentRepository documentRepository;

  @Override
  public void afterPropertiesSet() {
    meterRegistry.gauge(
        "documents.processed.successfully",
        documentRepository,
        DocumentRepository::countAllDocuments);
  }
}
