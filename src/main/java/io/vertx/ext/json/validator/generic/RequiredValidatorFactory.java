package io.vertx.ext.json.validator.generic;

import com.fasterxml.jackson.core.JsonPointer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class RequiredValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointerList scope, SchemaParser parser) {
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
  public boolean canCreateValidator(JsonObject schema) {
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
          if (!obj.containsKey(k)) throw new ValidationException(ValidationException.ErrorType.NO_MATCH); //TODO
        }
      }
    }
  }

}
