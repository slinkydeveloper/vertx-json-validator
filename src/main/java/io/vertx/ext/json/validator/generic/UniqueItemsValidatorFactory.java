package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.HashSet;

public class UniqueItemsValidatorFactory implements ValidatorFactory {

    private final static SyncValidator UNIQUE_VALIDATOR = (value) -> {
        if (value instanceof JsonArray) {
            JsonArray arr = (JsonArray)value;
            if (new HashSet(arr.getList()).size() != arr.size()) throw new ValidationException(ValidationException.ErrorType.NO_MATCH);
        }
    };

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            Boolean unique = (Boolean) schema.getValue("uniqueItems");
            if (unique) return UNIQUE_VALIDATOR;
            else return null;
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for uniqueItems keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null uniqueItems keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("uniqueItems");
    }

}
