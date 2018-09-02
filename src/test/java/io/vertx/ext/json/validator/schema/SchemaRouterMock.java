package io.vertx.ext.json.validator.schema;

import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaRouter;

import java.net.URI;

public class SchemaRouterMock implements SchemaRouter {
    @Override
    public Schema resolveSchema(URI uri) {
        return null;
    }

    @Override
    public void addSchema(Schema schema, URI scope, URI idKeyword) {

    }
}
