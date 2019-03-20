package io.vertx.ext.json.validator;

import io.vertx.core.VertxException;

public class NoSyncValidationException extends VertxException {

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
