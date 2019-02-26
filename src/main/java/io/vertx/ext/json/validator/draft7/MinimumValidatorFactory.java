package io.vertx.ext.json.validator.draft7;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.MinimumValidator;

public class MinimumValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number maximum = (Number) schema.getValue("minimum");
      return new MinimumValidator(maximum.doubleValue());
    } catch (ClassCastException e) {
      throw new SchemaException(schema, "Wrong type for minimum or exclusiveMinimum keyword", e);
    } catch (NullPointerException e) {
      throw new SchemaException(schema, "Null minimum or exclusiveMinimum keyword", e);
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("minimum");
  }

}
