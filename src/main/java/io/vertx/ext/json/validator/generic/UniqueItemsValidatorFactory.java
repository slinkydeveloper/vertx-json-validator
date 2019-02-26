package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.HashSet;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class UniqueItemsValidatorFactory implements ValidatorFactory {

  private final static BaseSyncValidator UNIQUE_VALIDATOR = new BaseSyncValidator() {
    @Override
    public void validateSync(Object in) throws ValidationException, NoSyncValidationException {
      if (in instanceof JsonArray) {
        JsonArray arr = (JsonArray) in;
        if (new HashSet(arr.getList()).size() != arr.size())
          throw createException("array elements must be unique", "uniqueItems", in);
      }
    }
  };

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator validator) {
    try {
      Boolean unique = (Boolean) schema.getValue("uniqueItems");
      if (unique) return UNIQUE_VALIDATOR;
      else return null;
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for uniqueItems keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null uniqueItems keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("uniqueItems");
  }

}
