package com.foobar.foobarchallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableRetry
@EnableAsync
public class FoobarChallengeApplication {

  public static void main(String[] args) {
    SpringApplication.run(FoobarChallengeApplication.class, args);
  }
}
