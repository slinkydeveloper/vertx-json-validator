package io.vertx.ext.json.validator;

import io.vertx.core.Future;
import io.vertx.ext.json.pointer.impl.JsonPointerList;

@FunctionalInterface
public interface Schema {
  default JsonPointerList getIds() {
    return new JsonPointerList();
  }

  Future validate(Object in);
}
