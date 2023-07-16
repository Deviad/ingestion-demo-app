package com.foobar.foobarchallenge.domain.model;

import com.foobar.foobarchallenge.common.ValueObject;

public record AlternativeName(String name) implements ValueObject {
  @Override
  public String toString() {
    return name;
  }
}
