package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class BaseFormatValidatorFactory implements ValidatorFactory {

    class FormatValidator implements SyncValidator {

        Predicate<String> validator;

        public FormatValidator(Predicate<String> validator) {
            this.validator = validator;
        }

        @Override
        public void validate(Object value) throws ValidationException {
            if (value instanceof String) {
                if (!validator.test((String)value)) {
                    throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
                }
            }
        }
    }

    protected Map<String, Predicate<String>> formats;
    protected List<String> ignoringFormats;

    public BaseFormatValidatorFactory() {
        this.formats = initFormatsMap();
        this.ignoringFormats = initIgnoringFormats();
    }

    protected List<String> initIgnoringFormats() {
        return Arrays.asList(
                "int32",
                "int64",
                "float",
                "double"
        );
    }

    public abstract Map<String, Predicate<String>> initFormatsMap();

    public void addStringFormatValidator(String formatName, Predicate<String> validator){
        this.formats.put(formatName, validator);
    }

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        String format = schema.getString("format");
        if (ignoringFormats.contains(format)) return null;
        else {
            Predicate<String> v = formats.get(format);
            if (v == null) throw new SchemaException("No format");
            else return new FormatValidator(v);
        }
    }
}
