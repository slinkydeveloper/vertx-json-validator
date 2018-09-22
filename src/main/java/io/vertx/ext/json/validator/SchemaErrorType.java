package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.VertxGen;

@VertxGen
public enum SchemaErrorType {
  /**
   * Keyword has wrong type or not valid value
   */
  WRONG_KEYWORD_VALUE,
  /**
   * The keyword has a null value
   */
  NULL_KEYWORD_VALUE;

  public SchemaException createException(Object schema, String message) {
    return new SchemaException(message, schema, this);
  }
}
