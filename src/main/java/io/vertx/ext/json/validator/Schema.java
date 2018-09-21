package io.vertx.ext.json.validator;

import io.vertx.core.Future;
import io.vertx.ext.json.pointer.JsonPointer;

@FunctionalInterface
public interface Schema {

  /**
   * Get scope of this schema
   *
   * @return
   */
  default JsonPointer getScope() {
    return JsonPointer.create();
  }

  /**
   * Return a Future that succeed when the validation succeed, while fail with a {@link ValidationException} when validation fails
   *
   * @param in
   * @return
   */
  Future validate(Object in);
}
