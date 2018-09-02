package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.SchemaErrorType;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.Validator;
import io.vertx.ext.json.validator.ValidatorFactory;
import io.vertx.ext.json.validator.generic.ExclusiveMaximumValidator;
import io.vertx.ext.json.validator.generic.MaximumValidator;

import java.net.URI;

public class MinimumValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            Number maximum = (Number) schema.getValue("minimum");
            if (schema.containsKey("exclusiveMinimum") && schema.getBoolean("exclusiveMinimum"))
                return new ExclusiveMaximumValidator(maximum.doubleValue());
            return new MaximumValidator(maximum.doubleValue());
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for minimum or exclusiveMinimum keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null minimum or exclusiveMinimum keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("minimum");
    }

}
