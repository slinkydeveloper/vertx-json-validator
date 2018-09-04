package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.*;

import java.net.URI;

public class MinPropertiesValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, JsonPointerList scope, SchemaParser parser) {
        try {
            Number minimum = (Number) schema.getValue("minProperties");
            if (minimum.intValue() < 0) throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "minProperties must be >= 0");
            return new MinPropertiesValidator(minimum.intValue());
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for minProperties keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null minProperties keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("minProperties");
    }

    public class MinPropertiesValidator implements SyncValidator {
        private final int minimum;
        public MinPropertiesValidator(int minimum) { this.minimum = minimum; }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof JsonObject) {
                if (((JsonObject)value).size() < minimum) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

}
