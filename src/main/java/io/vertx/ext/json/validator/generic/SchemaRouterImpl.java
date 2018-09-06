package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerImpl;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaRouter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SchemaRouterImpl implements SchemaRouter {

    final Map<JsonPointer, Schema> schemas;

    public SchemaRouterImpl() {
        schemas = new HashMap<>();
    }

    @Override
    public Schema resolveCachedSchema(JsonPointer pointer, JsonPointerList scope) {
        JsonPointerImpl jp = (JsonPointerImpl) pointer;
        URI u = jp.buildURI();
        if (!u.isAbsolute()) {
            // Fragment pointer or path pointer!
            if ((u.getSchemeSpecificPart() == null || u.getSchemeSpecificPart().isEmpty()) && u.getFragment() != null) {
                return scope
                        .stream()
                        .map(j -> new JsonPointerImpl(URIUtils.replaceFragment(((JsonPointerImpl)j).getStartingUri(), u.getFragment())))
                        .map(schemas::get)
                        .findFirst()
                        .orElse(null);
            } else {
                return scope
                        .stream()
                        .map(j -> new JsonPointerImpl(j.buildURI().resolve(u)))
                        .map(schemas::get)
                        .findFirst()
                        .orElse(null);
            }
        } else {
            return schemas.get(pointer);
        }
    }

    @Override
    public void addSchema(Schema schema, JsonPointerList scope) {
        scope.forEach(p -> schemas.put(p, schema));
    }
}
