package com.foobar.foobarchallenge.spring.configuration;

import com.foobar.foobarchallenge.spring.property.ApplicationDocumentServiceProperties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalDocumentServiceConfig {

  @Bean
  @Qualifier("documentServiceExecutor")
  Executor documentServiceExecutor(
      ApplicationDocumentServiceProperties localDocumentServiceProperties) {

    //    return Executors.newFixedThreadPool(localDocumentServiceProperties.threadPoolSize());
    // using virtual threads improves the performance by 16%
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
