package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class MinPropertiesValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
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

  public class MinPropertiesValidator implements SyncValidator {
    private final int minimum;

    public MinPropertiesValidator(int minimum) {
      this.minimum = minimum;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value instanceof JsonObject) {
        if (((JsonObject) value).size() < minimum) {
          throw NO_MATCH.createException("provided object should have size >= " + minimum, "minProperties", value);
        }
      }
    }
  }

}
