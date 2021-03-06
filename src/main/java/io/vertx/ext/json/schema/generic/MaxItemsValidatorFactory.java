package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class MaxItemsValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Number maximum = (Number) schema.getValue("maxItems");
      if (maximum.intValue() < 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "maxItems must be >= 0");
      return new MaxItemsValidator(maximum.intValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for maxItems keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null maxItems keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("maxItems");
  }

  public class MaxItemsValidator implements SyncValidator {
    private final int maximum;

    public MaxItemsValidator(int maximum) {
      this.maximum = maximum;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value instanceof JsonArray) {
        if (((JsonArray) value).size() > maximum) {
          throw NO_MATCH.createException("provided array should have size <= " + maximum, "maxItems", value);
        }
      }
    }
  }

}
