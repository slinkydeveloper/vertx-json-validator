package io.vertx.ext.json.validator;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;

public interface ValidatorFactory {
  /**
   * This method consume the schema eventually creating a new {@link Validator}. The schema parser calls it during schema parsing only if {@link #canConsumeSchema(JsonObject)} returns true
   *
   * @param schema JsonObject representing the schema
   * @param scope scope of the parsed schema
   * @param parser caller parser
   * @return
   */
  Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser);

  /**
   * Returns true if this factory can consume the provided schema, eventually returning an instance of {@link Validator}
   * @param schema
   * @return
   */
  boolean canConsumeSchema(JsonObject schema);
}
