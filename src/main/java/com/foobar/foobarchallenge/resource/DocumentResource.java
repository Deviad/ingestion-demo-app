package com.foobar.foobarchallenge.resource;

import com.foobar.foobarchallenge.application.service.ApplicationDocumentServiceImpl;
import com.foobar.foobarchallenge.port.adapter.mapper.DocumentMapper;
import com.foobar.foobarchallenge.resource.in.InDocumentDto;
import com.foobar.foobarchallenge.resource.out.OutDocumentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DocumentResource {

  private final ApplicationDocumentServiceImpl service;
  private final DocumentMapper mapper;

  @PostMapping(
      value = "/documents",
      consumes = MediaType.APPLICATION_XML_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<Void> processXmlDocument(@RequestBody List<InDocumentDto> documents) {
    service.addDocuments(documents.stream().map(mapper::mapDtoToDocumentSchema).toList());
    return ResponseEntity.accepted().build();
  }

  @GetMapping(
      value = "/documents",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<OutDocumentDto> getDocumentsByIds(@RequestParam List<String> companyIds) {
    return ResponseEntity.ok(service.getDocumentsByCompanyIds(companyIds));
  }
}
