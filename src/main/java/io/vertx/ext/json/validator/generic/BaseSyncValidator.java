package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.SyncValidator;
import io.vertx.ext.json.validator.ValidatorPriority;

public abstract class BaseSyncValidator implements SyncValidator {

  @Override
  public boolean isSync() {
    return true;
  }

  @Override
  public ValidatorPriority getPriority() {
    return ValidatorPriority.MIN_PRIORITY;
  }

}
