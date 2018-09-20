package io.vertx.ext.json.validator;

import io.vertx.core.Future;
import io.vertx.ext.json.pointer.JsonPointer;

public interface SchemaRouter {

  Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer schemaScope);
  Future<Schema> resolveRef(JsonPointer pointer, JsonPointer scope, SchemaParser schemaParser);
  void addSchema(Schema schema, JsonPointer actualPointer);

}
