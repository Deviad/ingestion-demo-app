package com.foobar.foobarchallenge.domain.model;

import com.foobar.foobarchallenge.common.ValueObject;

public record Name(String name) implements ValueObject {
  @Override
  public String toString() {
    return name;
  }
}
