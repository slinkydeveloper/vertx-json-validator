package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.json.pointer.JsonPointer;

@VertxGen
public interface Schema extends MutableStateValidator {

  /**
   * Get scope of this schema
   *
   * @return
   */
  JsonPointer getScope();

  /**
   * Get Json representation of the schema
   *
   * @return
   */
  Object getJson();

}
