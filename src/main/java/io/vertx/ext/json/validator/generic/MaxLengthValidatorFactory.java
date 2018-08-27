package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;

public class MaxLengthValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            Number maximum = (Number) schema.getValue("maxLength");
            if (maximum.intValue() < 0) throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "maxLength must be >= 0");
            return new MaxLengthValidator(maximum.intValue());
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for maxLength keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null maxLength keyword");
        }
    }

    public class MaxLengthValidator implements SyncValidator {
        private final int maximum;
        public MaxLengthValidator(int maximum) { this.maximum = maximum; }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof String) {
                if (((String)value).length() > maximum) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

}
