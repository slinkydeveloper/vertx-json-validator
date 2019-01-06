package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.ExclusiveMaximumValidator;
import io.vertx.ext.json.validator.generic.MaximumValidator;

public class MaximumValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number maximum = (Number) schema.getValue("maximum");
      if (schema.containsKey("exclusiveMaximum") && schema.getBoolean("exclusiveMaximum"))
        return new ExclusiveMaximumValidator(maximum.doubleValue());
      return new MaximumValidator(maximum.doubleValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for maximum or exclusiveMaximum keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null maximum or exclusiveMaximum keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("maximum");
  }

}
