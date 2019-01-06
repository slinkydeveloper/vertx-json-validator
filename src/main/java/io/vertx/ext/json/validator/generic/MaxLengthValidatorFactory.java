package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class MaxLengthValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number maximum = (Number) schema.getValue("maxLength");
      if (maximum.intValue() < 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "maxLength must be >= 0");
      return new MaxLengthValidator(maximum.intValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for maxLength keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null maxLength keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("maxLength");
  }

  public class MaxLengthValidator extends BaseSyncValidator {
    private final int maximum;

    public MaxLengthValidator(int maximum) {
      this.maximum = maximum;
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      if (in instanceof String) {
        if (((String) in).codePointCount(0, ((String) in).length()) > maximum) {
          throw NO_MATCH.createException("provided string should have size <= " + maximum, "maxLength", in);
        }
      }
    }
  }

}
