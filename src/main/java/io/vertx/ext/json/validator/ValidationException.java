package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.VertxException;
import io.vertx.ext.json.pointer.JsonPointer;

/**
 * This is the main class for every Validation flow related errors
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ValidationException extends VertxException {

  final private String keyword;
  final private Object input;
  final private ValidationErrorType errorType;
  private Schema schema;
  private JsonPointer scope;

  protected ValidationException(String message, String keyword, Object input, ValidationErrorType errorType) {
    super(message);
    this.keyword = keyword;
    this.input = input;
    this.errorType = errorType;
  }

  protected ValidationException(String message, Throwable cause, String keyword, Object input, ValidationErrorType errorType) {
    super(message, cause);
    this.keyword = keyword;
    this.input = input;
    this.errorType = errorType;
  }

  @Nullable public String keyword() {
    return keyword;
  }

  public Object input() {
    return input;
  }

  public ValidationErrorType errorType() {
    return errorType;
  }

  public Schema schema() {
    return schema;
  }

  public JsonPointer scope() {
    return scope;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public void setScope(JsonPointer scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    return "ValidationException{" +
        "message='" + getMessage() + '\'' +
        ", keyword='" + keyword + '\'' +
        ", input=" + input +
        ", errorType=" + errorType +
        ", schema=" + schema +
        ", scope=" + scope +
        '}';
  }
}
