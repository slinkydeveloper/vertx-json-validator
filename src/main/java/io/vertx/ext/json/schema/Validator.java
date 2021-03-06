package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.VertxGen;

@VertxGen
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
