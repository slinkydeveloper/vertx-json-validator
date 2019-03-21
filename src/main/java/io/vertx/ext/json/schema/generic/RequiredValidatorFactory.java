package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import java.util.HashSet;
import java.util.Set;

import static io.vertx.ext.json.schema.ValidationErrorType.NO_MATCH;

public class RequiredValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonArray keys = (JsonArray) schema.getValue("required");
      return new RequiredValidator(new HashSet(keys.getList()));
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for enum keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null enum keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("required");
  }

  public class RequiredValidator implements SyncValidator {
    private final Set<String> requiredKeys;

    public RequiredValidator(Set<String> requiredKeys) {
      this.requiredKeys = requiredKeys;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value instanceof JsonObject) {
        JsonObject obj = (JsonObject) value;
        for (String k : requiredKeys) {
          if (!obj.containsKey(k)) throw NO_MATCH.createException("provided object should contain property " + k, "required", value);
        }
      }
    }
  }

}
