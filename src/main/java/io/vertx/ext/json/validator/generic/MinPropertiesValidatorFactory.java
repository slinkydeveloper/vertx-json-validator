package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class MinPropertiesValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number minimum = (Number) schema.getValue("minProperties");
      if (minimum.intValue() < 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "minProperties must be >= 0");
      return new MinPropertiesValidator(minimum.intValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for minProperties keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null minProperties keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("minProperties");
  }

  public class MinPropertiesValidator extends BaseSyncValidator {
    private final int minimum;

    public MinPropertiesValidator(int minimum) {
      this.minimum = minimum;
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      if (in instanceof JsonObject) {
        if (((JsonObject) in).size() < minimum) {
          throw createException("provided object should have size >= " + minimum, "minProperties", in);
        }
      }
    }
  }

}
