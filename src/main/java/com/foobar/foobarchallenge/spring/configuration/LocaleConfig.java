package com.foobar.foobarchallenge.spring.configuration;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LocaleConfig {

  @PostConstruct
  public void init() {

    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    log.info(String.format("Date in UTC: %s", new Date()));
  }
}
