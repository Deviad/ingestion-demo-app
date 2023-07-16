package com.foobar.foobarchallenge.common;

public class CaseInsensitiveKey<T extends ValueObject> {
  private final String key;

  public CaseInsensitiveKey(T vo) {
    this.key = vo.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CaseInsensitiveKey<T> other = (CaseInsensitiveKey) obj;
    return key.equalsIgnoreCase(other.key);
  }

  @Override
  public int hashCode() {
    return key.toLowerCase().hashCode();
  }
}
