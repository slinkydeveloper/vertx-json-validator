package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class EnumValidatorFactory implements ValidatorFactory {

    @SuppressWarnings("unchecked")
    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            JsonArray allowedValues = (JsonArray) schema.getValue("enum");
            Set allowedValuesParsed = (Set) allowedValues
                    .getList().stream()
                    .map(o ->
                            (o instanceof Map) ? new JsonObject((Map<String, Object>)o) :
                            (o instanceof List) ? new JsonArray((List)o) :
                            o
                    ).collect(Collectors.toSet());
            return new EnumValidator(allowedValuesParsed);
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for enum keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null enum keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("enum");
    }

    public class EnumValidator implements SyncValidator {
        private final Set allowedValues;

        public EnumValidator(Set allowedValues) {
            this.allowedValues = allowedValues;
        }

        @Override
        public void validate(Object value) throws ValidationException {
            if (!allowedValues.contains(value)) throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
        }
    }

}
