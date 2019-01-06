package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.ext.json.validator.ValidationException;
import io.vertx.ext.json.validator.Validator;
import io.vertx.ext.json.validator.ValidatorPriority;

public abstract class BaseSyncValidator implements Validator {

  @Override
  public ValidatorPriority getPriority() {
    return ValidatorPriority.MIN_PRIORITY;
  }

  @Override
  public boolean isSync() {
    return true;
  }

  @Override
  public Future<Void> validateAsync(Object in) {
    try {
      validateSync(in);
      return Future.succeededFuture();
    } catch (ValidationException e) {
      return Future.failedFuture(e);
    }
  }
}
