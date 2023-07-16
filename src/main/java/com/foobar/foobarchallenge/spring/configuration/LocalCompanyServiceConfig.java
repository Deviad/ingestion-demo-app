package com.foobar.foobarchallenge.spring.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalCompanyServiceConfig {

  @Bean
  @Qualifier("companyServiceExecutor")
  Executor companyServiceExecutor() {
    // using virtual threads improves the performance by 16%
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
