package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.*;

import java.net.URI;

public class NullableValidatorFactory implements ValidatorFactory {

    private final static SyncValidator NULL_VALIDATOR = (value) -> {
        if (value == null) throw ValidationExceptionFactory.generateNotMatchValidationException(""); //TODO
    };

    @Override
    public Validator createValidator(JsonObject schema, JsonPointerList scope, SchemaParser parser) {
        try {
            Boolean nullable = (Boolean) schema.getValue("nullable");
            if (nullable == null || !nullable) return NULL_VALIDATOR;
            else return null;
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for nullable keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null nullable keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return true;
    }

}
