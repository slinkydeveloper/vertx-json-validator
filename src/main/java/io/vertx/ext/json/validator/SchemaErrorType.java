package io.vertx.ext.json.validator;

public enum SchemaErrorType {
  WRONG_KEYWORD_VALUE,
  NULL_KEYWORD_VALUE,
  UNABLE_TO_SOLVE_REF;

  public SchemaException createException(Object schema, String message) {
    return new SchemaException(message, schema, this);
  }
}
