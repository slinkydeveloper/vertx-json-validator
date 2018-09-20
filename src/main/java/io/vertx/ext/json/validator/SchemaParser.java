package io.vertx.ext.json.validator;

import io.vertx.ext.json.pointer.JsonPointer;

import java.net.URI;

public interface SchemaParser {

  Schema parse();
  Schema parse(Object json, JsonPointer scope);
  SchemaRouter getSchemaRouter();
  Schema parseSchemaFromString(String schema, URI uri);
}
