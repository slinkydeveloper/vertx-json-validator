package io.vertx.ext.json.validator;

public interface Validator {
  /**
   * Returns true if this validator is an instance of {@link AsyncValidator}
   *
   * @return
   */
  boolean isAsync();

  /**
   * Returns the priority of the validator
   *
   * @return
   */
  ValidatorPriority getPriority();
}
