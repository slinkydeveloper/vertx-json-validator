package io.vertx.ext.json.schema.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

public class NullableValidatorFactory implements ValidatorFactory {

  private final static SyncValidator NULL_VALIDATOR = (value) -> {
    if (value == null) throw ValidationErrorType.NO_MATCH.createException("input cannot be null", "nullable", value);
  };

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Boolean nullable = (Boolean) schema.getValue("nullable");
      if (nullable == null || !nullable) return NULL_VALIDATOR;
      else return null;
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for nullable keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null nullable keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return true;
  }

}
