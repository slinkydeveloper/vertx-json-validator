package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.SyncValidator;
import io.vertx.ext.json.validator.ValidationException;

public class ExclusiveMaximumValidator implements SyncValidator {
  private final double maximum;

  public ExclusiveMaximumValidator(double maximum) {
    this.maximum = maximum;
  }

  @Override
  public void validate(Object value) throws ValidationException {
    if (value instanceof Number) {
      if (((Number) value).doubleValue() >= maximum) {
        throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
      }
    }
  }
}
