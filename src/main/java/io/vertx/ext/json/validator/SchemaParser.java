package io.vertx.ext.json.validator;

import io.vertx.core.Future;
import io.vertx.ext.json.pointer.JsonPointer;

public interface SchemaParser {

  Schema parse();
  Schema parse(Object json, JsonPointer scope);
  SchemaRouter getSchemaRouter();
  Future<Schema> solveRef(JsonPointer pointer, JsonPointer scope);

}
