package io.vertx.ext.json.schema;

public class NoSyncValidationException extends RuntimeException {

  MutableStateValidator validator;

  public NoSyncValidationException(String message, MutableStateValidator validator) {
    super(message);
    this.validator = validator;
  }

  public NoSyncValidationException(String message, Throwable cause, MutableStateValidator validator) {
    super(message, cause);
    this.validator = validator;
  }

  public MutableStateValidator getValidator() {
    return validator;
  }
}
