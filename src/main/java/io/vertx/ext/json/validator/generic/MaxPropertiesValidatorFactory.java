package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.*;

import java.net.URI;

public class MaxPropertiesValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, JsonPointerList scope, SchemaParser parser) {
        try {
            Number maximum = (Number) schema.getValue("maxProperties");
            if (maximum.intValue() < 0) throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "maxProperties must be >= 0");
            return new MaxPropertiesValidator(maximum.intValue());
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for maxProperties keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null maxProperties keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("maxProperties");
    }

    public class MaxPropertiesValidator implements SyncValidator {
        private final int maximum;
        public MaxPropertiesValidator(int maximum) { this.maximum = maximum; }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof JsonObject) {
                if (((JsonObject)value).size() > maximum) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

}
