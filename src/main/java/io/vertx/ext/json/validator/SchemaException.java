package io.vertx.ext.json.validator;

import io.vertx.core.VertxException;

public class SchemaException extends VertxException {

  Object schema;
  SchemaErrorType errorType;

  public SchemaException(String message, Object schema, SchemaErrorType errorType) {
    super(message);
    this.schema = schema;
    this.errorType = errorType;
  }
}
