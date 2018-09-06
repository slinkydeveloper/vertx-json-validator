package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.SyncValidator;
import io.vertx.ext.json.validator.ValidationException;

public class ExclusiveMinimumValidator implements SyncValidator {
  private final double minimum;

  public ExclusiveMinimumValidator(double minimum) {
    this.minimum = minimum;
  }

  @Override
  public void validate(Object value) throws ValidationException {
    if (value instanceof Number) {
      if (((Number) value).doubleValue() <= minimum) {
        throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
      }
    }
  }
}
