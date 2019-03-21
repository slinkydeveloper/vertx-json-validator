package io.vertx.ext.json.schema.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.SchemaErrorType;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.Validator;
import io.vertx.ext.json.schema.ValidatorFactory;
import io.vertx.ext.json.schema.generic.ExclusiveMinimumValidator;
import io.vertx.ext.json.schema.generic.MinimumValidator;

public class MinimumValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Number maximum = (Number) schema.getValue("minimum");
      if (schema.containsKey("exclusiveMinimum") && schema.getBoolean("exclusiveMinimum"))
        return new ExclusiveMinimumValidator(maximum.doubleValue());
      return new MinimumValidator(maximum.doubleValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for minimum or exclusiveMinimum keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null minimum or exclusiveMinimum keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("minimum");
  }

}
