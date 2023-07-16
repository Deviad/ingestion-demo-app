package com.foobar.foobarchallenge.port.adapter.mapper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.foobar.foobarchallenge.common.Adapter;
import com.foobar.foobarchallenge.domain.model.DocumentSchema;
import com.foobar.foobarchallenge.resource.in.InDocumentDto;
import com.foobar.foobarchallenge.resource.out.OutDocumentDto;
import io.vavr.control.Try;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.springframework.beans.factory.annotation.Qualifier;

@Adapter
@RequiredArgsConstructor
@Slf4j
public class DocumentMapper {
  @Qualifier("documentXmlMapper")
  private final XmlMapper xmlMapper;

  public Document mapToLuceneDocument(DocumentSchema schema) {
    Document document = new Document();

    /*
     TextField allows more advanced queries (kind of like operator)
     StringField is for atomic values.
    */
    document.add(new TextField("title", Optional.of(schema.title()).orElse(""), Field.Store.YES));
    document.add(new StringField("id", Optional.of(schema.id()).orElse(""), Field.Store.YES));
    document.add(
        new TextField("date", Optional.of(schema.date().toString()).orElse(""), Field.Store.YES));
    document.add(new TextField("text", Optional.of(schema.text()).orElse(""), Field.Store.YES));
    document.add(new TextField("source", Optional.of(schema.source()).orElse(""), Field.Store.YES));
    log.debug("Document with title: {} created, ready to be indexed", schema.title());
    return document;
  }

  public Optional<DocumentSchema> parse(File file) {

    var documentSchemaEither =
        Try.of(() -> Optional.of(xmlMapper.readValue(file, DocumentSchema.class))).toEither();

    return documentSchemaEither.fold(
        error -> {
          log.error(
              "Parse error in DocumentParser:parse when parsing {} : {}",
              file.getName(),
              error.getMessage());
          return Optional.empty();
        },
        value -> value);
  }

  public DocumentSchema mapDtoToDocumentSchema(InDocumentDto inDocumentDto) {
    return DocumentSchema.of(
        inDocumentDto.date(),
        inDocumentDto.id(),
        inDocumentDto.title(),
        inDocumentDto.source(),
        inDocumentDto.text());
  }

  public OutDocumentDto.NewsItem mapDocumentToDocumentSchema(Document document) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = LocalDate.parse(document.get("date"), dateTimeFormatter);
    return new OutDocumentDto.NewsItem(
        date,
        Optional.ofNullable(document.get("id")).orElse(""),
        Optional.ofNullable(document.get("title")).orElse(""),
        Optional.ofNullable(document.get("source")).orElse(""),
        Optional.ofNullable(document.get("text")).orElse(""));
  }
}
