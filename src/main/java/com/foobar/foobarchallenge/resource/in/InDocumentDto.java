package com.foobar.foobarchallenge.resource.in;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor(onConstructor = @__(@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)))
@JacksonXmlRootElement(localName = "news-items")
@Value
public class InDocumentDto {

  @JsonFormat(pattern = "yyyy-MM-dd")
  LocalDate date;

  @JacksonXmlProperty(isAttribute = true)
  String id;

  String title;
  String source;
  String text;
}
