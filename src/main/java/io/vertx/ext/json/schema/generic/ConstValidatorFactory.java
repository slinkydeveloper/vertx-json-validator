package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class ConstValidatorFactory implements ValidatorFactory {

  @SuppressWarnings("unchecked")
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
      Object allowedValue = schema.getValue("const");
      return new EnumValidator(allowedValue);
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("const");
  }

  public class EnumValidator implements SyncValidator {
    private final Object allowedValue;

    public EnumValidator(Object allowedValue) {
      this.allowedValue = allowedValue;
    }

    @Override
    public ValidatorPriority getPriority() {
      return ValidatorPriority.MAX_PRIORITY;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (allowedValue != null) {
        if (!allowedValue.equals(value))
          throw NO_MATCH.createException("Input doesn't match const: " + allowedValue, "const", value);
      } else if (value != null) throw NO_MATCH.createException("Input doesn't match const: " + allowedValue, "const", value);
    }
  }

}
