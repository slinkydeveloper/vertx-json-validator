package io.vertx.ext.json.validator;

import io.vertx.core.Future;
import io.vertx.ext.json.pointer.JsonPointer;

@FunctionalInterface
public interface Schema {
  default JsonPointer getScope() {
    return JsonPointer.create();
  }

  Future validate(Object in);
}
