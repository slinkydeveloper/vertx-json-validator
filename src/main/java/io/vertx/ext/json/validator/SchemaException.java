package io.vertx.ext.json.validator;

import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;

public class SchemaException extends VertxException {

    JsonObject schema;
    SchemaErrorType errorType;

    public SchemaException(String message, JsonObject schema, SchemaErrorType errorType) {
        super(message);
        this.schema = schema;
        this.errorType = errorType;
    }
}
