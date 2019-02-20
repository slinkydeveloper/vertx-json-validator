package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.VertxException;

/**
 * This class represents an error while parsing a {@link Schema}
 *
 * @author slinkydeveloper
 */
@VertxGen
public class SchemaException extends VertxException {

  private Object schema;
  private SchemaErrorType errorType;

  public SchemaException(String message, Object schema, SchemaErrorType errorType, Throwable cause) {
    super(message, cause);
    this.schema = schema;
    this.errorType = errorType;
  }

  public Object schema() {
    return schema;
  }

  public SchemaErrorType errorType() {
    return errorType;
  }

  @Override
  public String toString() {
    return "SchemaException{" +
        "message=\'" + getMessage() + "\'" +
        ", schema=" + schema +
        ", errorType=" + errorType.toString() +
        '}';
  }
}
