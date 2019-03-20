package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.vertx.ext.json.validator.ValidationException.createException;

public abstract class BaseFormatValidatorFactory implements ValidatorFactory {

  protected final static FormatPredicate URI_VALIDATOR = in -> {
    try {
      return URI.create(in).isAbsolute();
    } catch (IllegalArgumentException e) {
      return false;
    }
  };

  protected final static FormatPredicate URI_REFERENCE_VALIDATOR = in -> {
    try {
      URI.create(in);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  };

  protected final static FormatPredicate REGEX_VALIDATOR = in -> {
    try {
      Pattern.compile(in);
      return true;
    } catch (PatternSyntaxException e) {
      return false;
    }
  };

  protected final static FormatPredicate IDN_EMAIL_VALIDATOR = in -> {
    return true;
  };

  class FormatValidator extends BaseSyncValidator {

    FormatPredicate validator;

    public FormatValidator(FormatPredicate validator) {
      this.validator = validator;
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      if (in instanceof String) {
        if (!validator.isValid((String) in)) {
          throw createException("Provided value don't match pattern", "pattern", in);
        }
      }
    }
  }

  protected Map<String, FormatPredicate> formats;
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

  public abstract Map<String, FormatPredicate> initFormatsMap();

  public void addStringFormatValidator(String formatName, FormatPredicate validator) {
    this.formats.put(formatName, validator);
  }

  protected FormatPredicate createPredicateFromPattern(final Pattern pattern) {
    return (in) -> pattern.matcher(in).matches();
  }

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    String format = schema.getString("format");
    if (ignoringFormats.contains(format)) return null;
    else {
      FormatPredicate v = formats.get(format);
      if (v == null) throw new SchemaException(schema, "Format not supported");
      else return new FormatValidator(v);
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("format");
  }
}
