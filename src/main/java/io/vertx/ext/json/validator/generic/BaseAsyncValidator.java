package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.AsyncValidator;
import io.vertx.ext.json.validator.ValidatorPriority;

public abstract class BaseAsyncValidator implements AsyncValidator {

  @Override
  public boolean isSync() {
    return false;
  }

  @Override
  public ValidatorPriority getPriority() {
    return ValidatorPriority.MIN_PRIORITY;
  }

}
