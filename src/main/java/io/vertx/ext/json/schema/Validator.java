package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.VertxGen;

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

}
