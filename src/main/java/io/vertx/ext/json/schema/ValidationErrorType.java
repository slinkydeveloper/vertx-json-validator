package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.VertxGen;

/**
 * All errors type. You can get this values using {@link ValidationException#errorType()}
 */
@VertxGen
public enum ValidationErrorType {
  /**
   * The input doesn't match a specific keyword rule
   */
  NO_MATCH,
  /**
   * Unable to solve reference
   */
  REF_ERROR;

  public ValidationException createException(String message, String keyword, Object input) {
    return new ValidationException(message, keyword, input, this);
  }

  public ValidationException createException(String message, Throwable t, String keyword, Object input) {
    return new ValidationException(message, t, keyword, input, this);
  }
}
