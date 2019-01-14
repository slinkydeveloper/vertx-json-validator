package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

@VertxGen
public interface Validator {

  /**
   * Returns true if this validator can actually provide a synchronous validation
   *
   * @return
   */
  boolean isSync();

  /**
   * Returns the priority of the validator
   *
   * @return
   */
  ValidatorPriority getPriority();

  /**
   * Validate the provided value
   *
   * @param in
   * @throws ValidationException if the object is not valid
   * @throws NoSyncValidationException if no sync validation can be provided
   */
  void validateSync(Object in) throws ValidationException, NoSyncValidationException;

  /**
   * Return a Future that succeed when the validation succeed, while fail with a {@link ValidationException} when validation fails
   *
   * @param in
   * @return
   */
  Future<Void> validateAsync(Object in);
}
