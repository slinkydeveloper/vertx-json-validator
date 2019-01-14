package io.vertx.ext.json.validator;

import io.vertx.core.Future;

public interface AsyncValidator extends Validator {

  /**
   * Return a Future that succeed when the validation succeed, while fail with a {@link ValidationException} when validation fails
   *
   * @param in
   * @return
   */
  Future<Void> validateAsync(Object in);

}
