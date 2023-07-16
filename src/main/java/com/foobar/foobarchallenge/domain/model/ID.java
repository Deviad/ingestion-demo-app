package com.foobar.foobarchallenge.domain.model;

import com.foobar.foobarchallenge.common.ValueObject;

public record ID(String id) implements ValueObject {
  @Override
  public String toString() {
    return id;
  }
}
