package io.vertx.ext.json.validator;

import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.util.Map;

public interface SchemaParser {

    Schema parse();

    Schema parse(JsonObject json, URI scope);

    SchemaRouter getSchemaRouter();

}
