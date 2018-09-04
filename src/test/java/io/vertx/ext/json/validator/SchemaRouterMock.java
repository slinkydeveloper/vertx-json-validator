package io.vertx.ext.json.validator;

import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaRouter;

import java.net.URI;
import java.util.HashMap;
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
    public Schema resolveCachedSchema(JsonPointer pointer, JsonPointerList scope) {
        return null;
    }

    @Override
    public void addSchema(Schema schema, JsonPointerList scope) {
        scope.forEach(p -> schemas.put(p, schema));
    }
}
