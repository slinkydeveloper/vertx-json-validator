package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.VertxGen;

@FunctionalInterface
@VertxGen
public interface SyncValidator extends Validator {
  @Override
  default boolean isAsync() {
    return false;
  }

  @Override
  default ValidatorPriority getPriority() {
    return ValidatorPriority.MIN_PRIORITY;
  }

  /**
   * Validate the provided value
   *
   * @param value
   * @throws ValidationException
   */
  void validate(Object value) throws ValidationException;
}
