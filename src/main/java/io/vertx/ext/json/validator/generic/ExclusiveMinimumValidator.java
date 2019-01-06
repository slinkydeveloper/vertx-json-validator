package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.ValidationException;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class ExclusiveMinimumValidator extends BaseSyncValidator {
  private final double minimum;

  public ExclusiveMinimumValidator(double minimum) {
    this.minimum = minimum;
  }

  @Override
  public void validateSync(Object in) throws ValidationException {
    if (in instanceof Number) {
      if (((Number) in).doubleValue() <= minimum) {
        throw NO_MATCH.createException("value should be > " + minimum, "minimum", in);
      }
    }
  }
}
