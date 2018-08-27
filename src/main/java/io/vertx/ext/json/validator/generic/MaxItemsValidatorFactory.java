package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;

public class MaxItemsValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            Number maximum = (Number) schema.getValue("maxItems");
            if (maximum.intValue() < 0) throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "maxItems must be >= 0");
            return new MaxItemsValidator(maximum.intValue());
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for maxItems keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null maxItems keyword");
        }
    }

    public class MaxItemsValidator implements SyncValidator {
        private final int maximum;
        public MaxItemsValidator(int maximum) { this.maximum = maximum; }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof JsonArray) {
                if (((JsonArray)value).size() > maximum) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

}
