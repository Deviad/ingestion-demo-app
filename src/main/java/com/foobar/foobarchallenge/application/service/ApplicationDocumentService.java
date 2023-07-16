package com.foobar.foobarchallenge.application.service;

import com.foobar.foobarchallenge.resource.out.OutDocumentDto;
import java.util.List;

public interface ApplicationDocumentService {
  void index();

  OutDocumentDto getDocumentsByCompanyIds(List<String> ids);
}
