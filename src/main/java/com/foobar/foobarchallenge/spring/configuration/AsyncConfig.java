package com.foobar.foobarchallenge.spring.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

  // Configure the default thread pool
  @Bean
  public Executor taskExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
