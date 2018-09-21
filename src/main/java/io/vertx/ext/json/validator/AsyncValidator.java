package io.vertx.ext.json.validator;

import io.vertx.core.Future;

@FunctionalInterface
public interface AsyncValidator extends Validator {
  @Override
  default boolean isAsync() {
    return true;
  }

  @Override
  default ValidatorPriority getPriority() {
    return ValidatorPriority.MIN_PRIORITY;
  }

  /**
   * Return a Future that succeed when the validation succeed, while fail with a {@link ValidationException} when validation fails
   *
   * @param in
   * @return
   */
  Future validate(Object in);
}
