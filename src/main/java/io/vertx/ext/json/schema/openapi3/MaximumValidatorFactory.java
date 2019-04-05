package io.vertx.ext.json.schema.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;
import io.vertx.ext.json.schema.generic.ExclusiveMaximumValidator;
import io.vertx.ext.json.schema.generic.MaximumValidator;

public class MaximumValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number maximum = (Number) schema.getValue("maximum");
      if (schema.containsKey("exclusiveMaximum") && schema.getBoolean("exclusiveMaximum"))
        return new ExclusiveMaximumValidator(maximum.doubleValue());
      return new MaximumValidator(maximum.doubleValue());
    } catch (ClassCastException e) {
      throw new SchemaException(schema, "Wrong type for maximum or exclusiveMaximum keyword", e);
    } catch (NullPointerException e) {
      throw new SchemaException(schema, "Null maximum or exclusiveMaximum keyword", e);
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("maximum");
  }

}
