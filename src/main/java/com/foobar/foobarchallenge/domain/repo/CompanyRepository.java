package com.foobar.foobarchallenge.domain.repo;

import com.foobar.foobarchallenge.domain.model.AlternativeName;
import com.foobar.foobarchallenge.domain.model.ID;
import com.foobar.foobarchallenge.domain.model.Name;
import java.util.List;

public interface CompanyRepository {
  void index();

  Name getCompanyNameById(ID companyId);

  ID getCompanyIdByName(Name name);

  List<AlternativeName> getCompanyAlternativeNames(Name name);
}
