package com.foobar.foobarchallenge.spring.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "foobar.document.application-document-service")
@RequiredArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Getter
public class ApplicationDocumentServiceProperties {
  private final int batchSize;
}
