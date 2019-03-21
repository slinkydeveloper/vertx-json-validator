package io.vertx.ext.json.schema.generic;

import io.vertx.ext.json.schema.SyncValidator;
import io.vertx.ext.json.schema.ValidatorPriority;

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
