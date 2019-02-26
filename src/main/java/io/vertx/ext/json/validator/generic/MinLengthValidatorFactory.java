package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class MinLengthValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number minimum = (Number) schema.getValue("minLength");
      if (minimum.intValue() < 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "minLength must be >= 0");
      return new MinLengthValidator(minimum.intValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for minLength keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null minLength keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("minLength");
  }

  public class MinLengthValidator extends BaseSyncValidator {
    private final int minimum;

    public MinLengthValidator(int minimum) {
      this.minimum = minimum;
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      if (in instanceof String) {
        if (((String) in).codePointCount(0, ((String) in).length()) < minimum) {
          throw createException("provided string should have size >= " + minimum, "minLength", in);
        }
      }
    }
  }

}
