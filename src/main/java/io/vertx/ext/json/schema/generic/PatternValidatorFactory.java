package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class PatternValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
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

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("pattern");
  }

  public class PatternValidator implements SyncValidator {
    private final Pattern pattern;

    public PatternValidator(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value instanceof String) {
        Matcher m = pattern.matcher((String) value);
        if (!(m.matches() || m.lookingAt() || m.find())) {
          throw NO_MATCH.createException("provided string should respect pattern " + pattern, "pattern", value);
        }
      }
    }
  }

}
