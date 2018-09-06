package io.vertx.ext.json.validator;

import io.vertx.ext.json.pointer.impl.JsonPointerList;

public interface SchemaParser {

  Schema parse();

  Schema parse(Object json, JsonPointerList scope);

  SchemaRouter getSchemaRouter();

}
