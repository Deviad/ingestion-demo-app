package com.foobar.foobarchallenge.domain;

  public class Foobar extends RuntimeException {
    public enum ErrorCode {
      GENERAL,
      CANNOT_LOAD_ARTICLE_FILE,
      CANNOT_LOAD_COMPANY_FILE,
      CANNOT_CREATE_INDEX_READER,

    }

    private final ErrorCode errorCode;
    private final String[] parameters;

    public Foobar(String message) {
      this(message, ErrorCode.GENERAL);
    }

    public Foobar(Throwable cause) {
      this(cause, ErrorCode.GENERAL);
    }

    public Foobar(ErrorCode errorCode, String... parameters) {
      this.errorCode = errorCode;
      this.parameters = parameters;
    }

    public Foobar(String message, ErrorCode errorCode, String... parameters) {
      super(message);
      this.errorCode = errorCode;
      this.parameters = parameters;
    }

    public Foobar(
        String message, Throwable cause, ErrorCode errorCode, String... parameters) {
      super(message, cause);
      this.errorCode = errorCode;
      this.parameters = parameters;
    }

    public Foobar(Throwable cause, ErrorCode errorCode, String... parameters) {
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
