package io.vertx.ext.json.validator;

import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerList;

public interface SchemaRouter {

    Schema resolveCachedSchema(JsonPointer pointer, JsonPointerList scope);

    void addSchema(Schema schema, JsonPointerList scope);

}
