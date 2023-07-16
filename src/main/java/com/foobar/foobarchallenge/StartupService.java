package com.foobar.foobarchallenge;

import com.foobar.foobarchallenge.application.service.ApplicationDocumentService;
import com.foobar.foobarchallenge.domain.repo.CompanyRepository;
import com.foobar.foobarchallenge.domain.repo.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StartupService {
  private final DocumentRepository documentRepository;
  private final CompanyRepository companyRepository;
  private final ApplicationDocumentService apDocumentService;

  @EventListener
  public void start(ApplicationStartedEvent event) {
    log.info("Application started");
    companyRepository.index();
    apDocumentService.index();
    documentRepository.countAllDocuments();
    // this is just for demo purposes, to show that the indexing is really working
    documentRepository.getDocumentTitlesWithCompanyNameInText("ABB ltd").forEach(log::info);
  }
}
