package io.vertx.ext.json.validator;

import io.vertx.ext.json.pointer.JsonPointer;

public interface SchemaRouter {

  Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer schemaScope);

  void addSchema(Schema schema, JsonPointer actualPointer);

}
