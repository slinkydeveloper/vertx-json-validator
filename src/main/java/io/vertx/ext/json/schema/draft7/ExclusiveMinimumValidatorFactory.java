package io.vertx.ext.json.schema.draft7;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.SchemaErrorType;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.Validator;
import io.vertx.ext.json.schema.ValidatorFactory;
import io.vertx.ext.json.schema.generic.ExclusiveMinimumValidator;

public class ExclusiveMinimumValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Number maximum = (Number) schema.getValue("exclusiveMinimum");
      return new ExclusiveMinimumValidator(maximum.doubleValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for exclusiveMinimum keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null exclusiveMinimum keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("exclusiveMinimum");
  }

}
