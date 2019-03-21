package io.vertx.ext.json.schema.generic;

import io.vertx.ext.json.schema.SyncValidator;
import io.vertx.ext.json.schema.ValidationException;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class ExclusiveMinimumValidator implements SyncValidator {
  private final double minimum;

  public ExclusiveMinimumValidator(double minimum) {
    this.minimum = minimum;
  }

  @Override
  public void validate(Object value) throws ValidationException {
    if (value instanceof Number) {
      if (((Number) value).doubleValue() <= minimum) {
        throw NO_MATCH.createException("value should be > " + minimum, "minimum", value);
      }
    }
  }
}
