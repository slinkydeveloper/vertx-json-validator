package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class ConstValidatorFactory implements ValidatorFactory {

  @SuppressWarnings("unchecked")
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
      Object allowedValue = schema.getValue("const");
      return new EnumValidator(allowedValue);
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("const");
  }

  public class EnumValidator extends BaseSyncValidator {

    private final Object allowedValue;

    public EnumValidator(Object allowedValue) {
      this.allowedValue = allowedValue;
    }

    @Override
    public ValidatorPriority getPriority() {
      return ValidatorPriority.MAX_PRIORITY;
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      if (allowedValue != null) {
        if (!allowedValue.equals(in))
          throw createException("Input doesn't match const: " + allowedValue, "const", in);
      } else if (in != null) throw createException("Input doesn't match const: " + allowedValue, "const", in);
    }
  }

}
