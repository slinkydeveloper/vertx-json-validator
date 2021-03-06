package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class MaxPropertiesValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Number maximum = (Number) schema.getValue("maxProperties");
      if (maximum.intValue() < 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "maxProperties must be >= 0");
      return new MaxPropertiesValidator(maximum.intValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for maxProperties keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null maxProperties keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("maxProperties");
  }

  public class MaxPropertiesValidator implements SyncValidator {
    private final int maximum;

    public MaxPropertiesValidator(int maximum) {
      this.maximum = maximum;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value instanceof JsonObject) {
        if (((JsonObject) value).size() > maximum) {
          throw NO_MATCH.createException("provided object should have size <= " + maximum, "maxProperties", value);
        }
      }
    }
  }

}
