package com.foobar.foobarchallenge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.TestSocketUtils;

class FoobarExceptionChallengeApplicationTests {

  @Test
  void contextLoads() {

    Assertions.assertDoesNotThrow(
        () -> {
          int randomPort = TestSocketUtils.findAvailableTcpPort();
          FoobarChallengeApplication.main(
              new String[] {
                String.format("--server.port=%s", randomPort), "--spring.profiles.active=test"
              });
        });
  }
}
