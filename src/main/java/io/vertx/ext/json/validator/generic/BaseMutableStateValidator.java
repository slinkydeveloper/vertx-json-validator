package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.ext.json.validator.AsyncValidatorException;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.ValidationException;
import io.vertx.ext.json.validator.ValidatorPriority;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseMutableStateValidator implements MutableStateValidator {

  final AtomicBoolean isSync;
  private MutableStateValidator parent;

  public BaseMutableStateValidator(MutableStateValidator parent) {
    this.parent = parent;
    this.isSync = new AtomicBoolean(false);
  }

  public abstract boolean calculateIsSync();

  protected Future<Void> validateSyncAsAsync(Object in) {
    try {
      validateSync(in);
      triggerUpdateIsSync();
      return Future.succeededFuture();
    } catch (ValidationException e) {
      return Future.failedFuture(e);
    }
  }

  protected void initializeIsSync() {
    isSync.set(calculateIsSync());
  }

  @Override
  public void triggerUpdateIsSync() {
    isSync.set(calculateIsSync());
    if (getParent() != null)
      getParent().triggerUpdateIsSync();
  }

  @Override
  public MutableStateValidator getParent() { return parent; }

  protected void checkSync() throws ValidationException, AsyncValidatorException {
    if (!isSync()) throw new AsyncValidatorException();
  }

  @Override
  public boolean isSync() {
    return isSync.get();
  }

  @Override
  public ValidatorPriority getPriority() {
    return ValidatorPriority.MIN_PRIORITY;
  }
}