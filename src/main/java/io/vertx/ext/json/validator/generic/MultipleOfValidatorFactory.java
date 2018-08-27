package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;

public class MultipleOfValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            Number multipleOf = (Number) schema.getValue("multipleOf");
            return new MultipleOfValidator(multipleOf.doubleValue());
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for multipleOf keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null multipleOf keyword");
        }
    }

    class MultipleOfValidator implements SyncValidator {
        private final double multipleOf;
        public MultipleOfValidator(double multipleOf) { this.multipleOf = multipleOf; }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof Number) {
                if (((Number)value).doubleValue() % multipleOf != 0) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

}
