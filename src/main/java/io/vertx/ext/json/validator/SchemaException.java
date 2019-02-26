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

  public SchemaException(Object schema, String message, Throwable cause) {
    super(message, cause);
    this.schema = schema;
  }

  public SchemaException(Object schema, String message) {
    super(message);
    this.schema = schema;
  }

  public Object schema() {
    return schema;
  }

  @Override
  public String toString() {
    return "SchemaException{" +
        "message=\'" + getMessage() + "\'" +
        ", schema=" + schema +
        '}';
  }
}
