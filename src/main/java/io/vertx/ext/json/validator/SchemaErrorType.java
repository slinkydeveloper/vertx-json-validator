package io.vertx.ext.json.validator;

import io.vertx.core.json.JsonObject;

public enum SchemaErrorType {
    WRONG_KEYWORD_VALUE,
    NULL_KEYWORD_VALUE;

    public SchemaException createException(Object schema, String message) {
        return new SchemaException(message, schema, this);
    }
}
