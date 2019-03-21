package io.vertx.ext.json.schema.generic;

import io.vertx.ext.json.schema.SyncValidator;
import io.vertx.ext.json.schema.ValidationException;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class ExclusiveMaximumValidator implements SyncValidator {
  private final double maximum;

  public ExclusiveMaximumValidator(double maximum) {
    this.maximum = maximum;
  }

  @Override
  public void validate(Object value) throws ValidationException {
    if (value instanceof Number) {
      if (((Number) value).doubleValue() >= maximum) {
        throw NO_MATCH.createException("value should be < " + maximum, "maximum", value);
      }
    }
  }
}
