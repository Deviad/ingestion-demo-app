package com.foobar.foobarchallenge.port.adapter.mapper;

import static com.foobar.foobarchallenge.TestUtils.FIELDS_ARE_MAPPED_CORRECTLY;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.foobar.foobarchallenge.domain.model.DocumentSchema;
import com.foobar.foobarchallenge.spring.configuration.DocumentParserConfiguration;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Random;

import org.apache.lucene.document.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DocumentParserConfiguration.class)
@ActiveProfiles({"test"})
class DocumentMapperTest {
  @Autowired
  @Qualifier("documentXmlMapper")
  XmlMapper jackson;

  @Test
  void mapToLuceneDocument() {
    var mapper = new DocumentMapper(jackson);
    int n = new Random().nextInt();
    String fileName = String.format("article-doc-%s", n);
    File xml = null;
    try {
      xml = File.createTempFile(fileName, ".xml");
      Files.write(xml.toPath(), FIELDS_ARE_MAPPED_CORRECTLY.getBytes());
      Optional<DocumentSchema> doc = mapper.parse(xml);
      Document indexableFields = mapper.mapToLuceneDocument(doc.orElseThrow());

      assertEquals("2014-03-14", indexableFields.getField("date").stringValue());
      assertEquals("0A01-5215-5B25-124E", indexableFields.getField("id").stringValue());
      assertEquals(" bp ", indexableFields.getField("text").stringValue());
      assertEquals("Digital Journal", indexableFields.getField("source").stringValue());


    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      xml.delete();
    }

    assertTrue(true);
  }
}
