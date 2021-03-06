package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class MultipleOfValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Number multipleOf = (Number) schema.getValue("multipleOf");
      return new MultipleOfValidator(multipleOf.doubleValue());
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for multipleOf keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null multipleOf keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("multipleOf");
  }

  class MultipleOfValidator implements SyncValidator {
    private final double multipleOf;

    public MultipleOfValidator(double multipleOf) {
      this.multipleOf = multipleOf;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value instanceof Number) {
        if (((Number) value).doubleValue() % multipleOf != 0) {
          throw NO_MATCH.createException("provided number should be multiple of " + multipleOf, "multipleOf", value);
        }
      }
    }
  }

}
