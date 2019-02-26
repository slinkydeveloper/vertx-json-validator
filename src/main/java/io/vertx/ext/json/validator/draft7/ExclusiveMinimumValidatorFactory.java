package io.vertx.ext.json.validator.draft7;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.ExclusiveMinimumValidator;

public class ExclusiveMinimumValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number maximum = (Number) schema.getValue("exclusiveMinimum");
      return new ExclusiveMinimumValidator(maximum.doubleValue());
    } catch (ClassCastException e) {
      throw new SchemaException(schema, "Wrong type for exclusiveMinimum keyword", e);
    } catch (NullPointerException e) {
      throw new SchemaException(schema, "Null exclusiveMinimum keyword", e);
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("exclusiveMinimum");
  }

}
