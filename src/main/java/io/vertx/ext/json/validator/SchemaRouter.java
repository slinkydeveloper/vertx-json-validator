package io.vertx.ext.json.validator;

import java.net.URI;

public interface SchemaRouter {

    Schema resolveSchema(URI uri);

    void addSchema(Schema schema, URI scope, URI idKeyword);

}
