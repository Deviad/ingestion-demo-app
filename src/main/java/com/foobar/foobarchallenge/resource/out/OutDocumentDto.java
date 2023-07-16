package com.foobar.foobarchallenge.resource.out;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)))
@JacksonXmlRootElement(localName = "news-items")
public class OutDocumentDto {

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "news-item")
  List<NewsItem> newsItems;

  @Value
  @AllArgsConstructor(onConstructor = @__(@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)))
  public static class NewsItem {

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    @JacksonXmlProperty(isAttribute = true)
    String id;

    String title;

    String source;

    String text;
  }
}
