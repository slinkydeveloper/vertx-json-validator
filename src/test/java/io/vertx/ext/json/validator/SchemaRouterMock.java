package io.vertx.ext.json.validator;

import io.vertx.core.Future;
import io.vertx.ext.json.pointer.JsonPointer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaRouterMock implements SchemaRouter {

  Map<JsonPointer, Schema> schemas;

  public SchemaRouterMock() {
    this.schemas = new HashMap<>();
  }

  public Map<JsonPointer, Schema> getSchemas() {
    return schemas;
  }

  @Override
  public Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer schemaScope, SchemaParser parser) {
    return null;
  }

  @Override
  public Future<Schema> resolveRef(JsonPointer pointer, JsonPointer scope, SchemaParser schemaParser) {
    return null;
  }

  @Override
  public void addSchema(Schema schema, JsonPointer scope) {
    schemas.put(scope, schema);
  }

  @Override
  public List<Schema> registeredSchemas() {
    return new ArrayList<>();
  }
}
