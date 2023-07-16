package com.foobar.foobarchallenge.domain;

  public class FoobarException extends RuntimeException {
    public enum ErrorCode {
      GENERAL,
      CANNOT_LOAD_ARTICLE_FILE,
      CANNOT_LOAD_COMPANY_FILE,
      CANNOT_CREATE_INDEX_READER,

    }

    private final ErrorCode errorCode;
    private final String[] parameters;

    public FoobarException(String message) {
      this(message, ErrorCode.GENERAL);
    }

    public FoobarException(Throwable cause) {
      this(cause, ErrorCode.GENERAL);
    }

    public FoobarException(ErrorCode errorCode, String... parameters) {
      this.errorCode = errorCode;
      this.parameters = parameters;
    }

    public FoobarException(String message, ErrorCode errorCode, String... parameters) {
      super(message);
      this.errorCode = errorCode;
      this.parameters = parameters;
    }

    public FoobarException(
        String message, Throwable cause, ErrorCode errorCode, String... parameters) {
      super(message, cause);
      this.errorCode = errorCode;
      this.parameters = parameters;
    }

    public FoobarException(Throwable cause, ErrorCode errorCode, String... parameters) {
      super(cause);
      this.errorCode = errorCode;
      this.parameters = parameters;
    }

    public ErrorCode getErrorCode() {
      return errorCode;
    }

    public String[] getParameters() {
      return parameters;
    }
  }
