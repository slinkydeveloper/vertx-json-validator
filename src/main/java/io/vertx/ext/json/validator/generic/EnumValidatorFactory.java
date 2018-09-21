package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class EnumValidatorFactory implements ValidatorFactory {

  @SuppressWarnings("unchecked")
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonArray allowedValues = (JsonArray) schema.getValue("enum");
      Set allowedValuesParsed = (Set) allowedValues
          .getList().stream()
          .map(o ->
              (o instanceof Map) ? new JsonObject((Map<String, Object>) o) :
                  (o instanceof List) ? new JsonArray((List) o) :
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
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("enum");
  }

  public class EnumValidator implements SyncValidator {
    private final Set allowedValues;

    public EnumValidator(Set allowedValues) {
      this.allowedValues = allowedValues;
    }

    @Override
    public ValidatorPriority getPriority() {
      return ValidatorPriority.MAX_PRIORITY;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (!allowedValues.contains(value)) throw NO_MATCH.createException("Input doesn't match one of allowed values of enum: " + allowedValues, "enum", value);
    }
  }

}
