package com.foobar.foobarchallenge.domain.service;

import com.foobar.foobarchallenge.common.ApplicationService;
import com.foobar.foobarchallenge.common.DomainService;
import com.foobar.foobarchallenge.domain.repo.CompanyRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@ApplicationService
@DomainService
class LocalCompanyService implements CompanyService {

  private final CompanyRepository localCompanyRepository;

  @Override
  @Timed(value = "companies.index.execution.time", description = "Time taken to execute index")
  public void index() {
    localCompanyRepository.index();
  }
}
