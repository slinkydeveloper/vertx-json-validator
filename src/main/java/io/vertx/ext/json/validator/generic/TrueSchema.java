package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

public class TrueSchema implements Schema {

  private static class TrueSchemaHolder {
    static final TrueSchema INSTANCE = new TrueSchema(null);
  }

  public static TrueSchema getInstance() {
    return TrueSchemaHolder.INSTANCE;
  }

  MutableStateValidator parent;

  public TrueSchema(MutableStateValidator parent) {
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
  public void validateSync(Object in) throws ValidationException, AsyncValidatorException { }

  @Override
  public Future<Void> validateAsync(Object in) {
    return Future.succeededFuture();
  }

  @Override
  public JsonPointer getScope() {
    return JsonPointer.create();
  }

  @Override
  public MutableStateValidator getParent() {
    return parent;
  }

  @Override
  public void triggerUpdateIsSync() { }

}
