package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.ValidationException;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class ExclusiveMaximumValidator extends BaseSyncValidator {
  private final double maximum;

  public ExclusiveMaximumValidator(double maximum) {
    this.maximum = maximum;
  }

  @Override
  public void validateSync(Object in) throws ValidationException {
    if (in instanceof Number) {
      if (((Number) in).doubleValue() >= maximum) {
        throw NO_MATCH.createException("value should be < " + maximum, "maximum", in);
      }
    }
  }
}
