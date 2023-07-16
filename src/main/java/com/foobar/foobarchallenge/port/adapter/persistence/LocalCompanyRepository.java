package com.foobar.foobarchallenge.port.adapter.persistence;

import com.foobar.foobarchallenge.common.CaseInsensitiveKey;
import com.foobar.foobarchallenge.common.Repository;
import com.foobar.foobarchallenge.domain.model.AlternativeName;
import com.foobar.foobarchallenge.domain.model.ID;
import com.foobar.foobarchallenge.domain.model.Name;
import com.foobar.foobarchallenge.domain.repo.CompanyRepository;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class LocalCompanyRepository implements CompanyRepository {

  private final CompanyRepositoryHelper companyRepositoryHelper;
  private final Map<ID, Name> companyRepo;
  private final Map<CaseInsensitiveKey<Name>, ID> reverseCompanyRepo;
  private final Map<CaseInsensitiveKey<Name>, List<AlternativeName>> companyAlternativeNames;

  LocalCompanyRepository(CompanyRepositoryHelper companyRepositoryHelper) {
    this.companyRepositoryHelper = companyRepositoryHelper;
    this.companyAlternativeNames = new ConcurrentHashMap<>();
    this.companyRepo = new ConcurrentHashMap<>();
    this.reverseCompanyRepo = new ConcurrentHashMap<>();
  }

  @Override
  @Timed(value = "companies.index.execution.time", description = "Time taken to execute index")
  public void index() {
    companyRepositoryHelper.index(companyRepo, reverseCompanyRepo, companyAlternativeNames);
  }

  @Override
  public Name getCompanyNameById(ID companyId) {
    return companyRepo.get(companyId);
  }

  @Override
  public ID getCompanyIdByName(Name name) {
    return reverseCompanyRepo.get(new CaseInsensitiveKey<>(name));
  }

  @Override
  public List<AlternativeName> getCompanyAlternativeNames(Name name) {
    CaseInsensitiveKey<Name> key = new CaseInsensitiveKey<>(name);
    return companyAlternativeNames.get(key);
  }
}
