package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.pointer.JsonPointer;

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

  /**
   * Return the default value defined in the schema
   *
   * @return
   */
  Object getDefaultValue();

  /**
   * Return true if the schema has a default value defined
   *
   * @return
   */
  boolean hasDefaultValue();

  /**
   * This function mutates obj applying default values in key/pair objects.
   *
   * @param obj
   */
  void applyDefaultValues(Object obj);

}
