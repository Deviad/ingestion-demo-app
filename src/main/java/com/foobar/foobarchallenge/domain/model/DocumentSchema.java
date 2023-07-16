package com.foobar.foobarchallenge.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.time.LocalDate;
import lombok.*;

@Value
@AllArgsConstructor(
    staticName = "of",
    onConstructor = @__(@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)))
@JacksonXmlRootElement(localName = "news-item")
public class DocumentSchema {
  LocalDate date;

  @JacksonXmlProperty(isAttribute = true)
  String id;

  String title;
  String source;
  String text;
}
