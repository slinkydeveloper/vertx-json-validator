package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.SyncValidator;
import io.vertx.ext.json.validator.ValidationException;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class MaximumValidator implements SyncValidator {
  private final double maximum;

  public MaximumValidator(double maximum) {
    this.maximum = maximum;
  }

  @Override
  public void validate(Object value) throws ValidationException {
    if (value instanceof Number) {
      if (((Number) value).doubleValue() > maximum) {
        throw NO_MATCH.createException("value should be <= " + maximum, "maximum", value);
      }
    }
  }
}
