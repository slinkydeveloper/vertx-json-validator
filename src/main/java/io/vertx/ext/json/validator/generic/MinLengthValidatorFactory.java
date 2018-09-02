package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;

public class MinLengthValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            Number minimum = (Number) schema.getValue("minLength");
            if (minimum.intValue() < 0) throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "minLength must be >= 0");
            return new MinLengthValidator(minimum.intValue());
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for minLength keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null minLength keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("minLength");
    }

    public class MinLengthValidator implements SyncValidator {
        private final int minimum;
        public MinLengthValidator(int minimum) { this.minimum = minimum; }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof String) {
                if (((String)value).length() < minimum) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

}
