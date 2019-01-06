package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class MaxItemsValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
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

  public class MaxItemsValidator extends BaseSyncValidator {
    private final int maximum;

    public MaxItemsValidator(int maximum) {
      this.maximum = maximum;
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      if (in instanceof JsonArray) {
        if (((JsonArray) in).size() > maximum) {
          throw NO_MATCH.createException("provided array should have size <= " + maximum, "maxItems", in);
        }
      }
    }
  }

}
