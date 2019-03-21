package io.vertx.ext.json.schema.generic;

import io.vertx.ext.json.schema.AsyncValidator;
import io.vertx.ext.json.schema.ValidatorPriority;

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
