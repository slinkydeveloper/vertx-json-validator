package io.vertx.ext.json.schema.generic;

import io.vertx.core.Future;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import static io.vertx.ext.json.schema.ValidationException.createException;

public class FalseSchema implements Schema {

  private static class FalseSchemaHolder {
    static final FalseSchema INSTANCE = new FalseSchema(null);
  }

  public static FalseSchema getInstance() {
    return FalseSchemaHolder.INSTANCE;
  }

  MutableStateValidator parent;

  public FalseSchema(MutableStateValidator parent) {
    this.parent = parent;
  }

  @Override
  public boolean isSync() {
    return true;
  }

  @Override
  public ValidatorPriority getPriority() {
    return ValidatorPriority.MAX_PRIORITY;
  }

  @Override
  public void validateSync(Object in) throws ValidationException, NoSyncValidationException {
    throw createException("False schema always fail validation", null, in);
  }

  @Override
  public Future<Void> validateAsync(Object in) {
    return Future.failedFuture(createException("False schema always fail validation", null, in));
  }

  @Override
  public JsonPointer getScope() {
    return JsonPointer.create();
  }

  @Override
  public Boolean getJson() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public void applyDefaultValues(Object obj) { }

  @Override
  public MutableStateValidator getParent() {
    return parent;
  }

  @Override
  public void triggerUpdateIsSync() { }

}
