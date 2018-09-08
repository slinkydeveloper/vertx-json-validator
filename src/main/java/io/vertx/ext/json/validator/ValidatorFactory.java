package io.vertx.ext.json.validator;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;

public interface ValidatorFactory {
  /**
   * This method can return null!
   *
   * @param schema
   * @param scope
   * @param parser
   * @return
   */
  Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser);

  //TODO change name in something better...
  boolean canCreateValidator(JsonObject schema);
}
