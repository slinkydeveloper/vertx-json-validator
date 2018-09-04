package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaRouter;

import java.util.HashMap;
import java.util.Map;

public class SchemaRouterImpl implements SchemaRouter {

    final Map<JsonPointer, Schema> schemas;

    public SchemaRouterImpl() {
        schemas = new HashMap<>();
    }

    @Override
    public Schema resolveCachedSchema(JsonPointer pointer, JsonPointerList scope) {
        return null;
    }

    @Override
    public void addSchema(Schema schema, JsonPointerList scope) {

    }
}
