package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            String pattern = (String) schema.getValue("pattern");
            return new PatternValidator(Pattern.compile(pattern));
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for pattern keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null pattern keyword");
        } catch (PatternSyntaxException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Invalid pattern in pattern keyword");
        }
    }

    public class PatternValidator implements SyncValidator {
        private final Pattern pattern;
        public PatternValidator(Pattern pattern) { this.pattern = pattern; }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof String) {
                if (!pattern.matcher((String)value).matches()) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

}
