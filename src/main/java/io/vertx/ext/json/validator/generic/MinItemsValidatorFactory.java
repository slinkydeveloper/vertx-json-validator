package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class MinItemsValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number minimum = (Number) schema.getValue("minItems");
      if (minimum.intValue() < 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "minItems must be >= 0");
      return new MinItemsValidator(minimum.intValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for minItems keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null minItems keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("minItems");
  }

  public class MinItemsValidator extends BaseSyncValidator {
    private final int minimum;

    public MinItemsValidator(int minimum) {
      this.minimum = minimum;
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      if (in instanceof JsonArray) {
        if (((JsonArray) in).size() < minimum) {
          throw NO_MATCH.createException("provided array should have size >= " + minimum, "minItems", in);
        }
      }
    }
  }

}
