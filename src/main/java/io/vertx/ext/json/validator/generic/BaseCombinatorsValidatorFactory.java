package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCombinatorsValidatorFactory implements ValidatorFactory {
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      JsonArray allOfSchemas = schema.getJsonArray(getKeyword());
      if (allOfSchemas.size() == 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, getKeyword() + " must have at least one element");
      JsonPointer basePointer = scope.append(getKeyword());
      List<Schema> parsedSchemas = new ArrayList<>();

      BaseCombinatorsValidator validator = instantiate(parent);
      for (int i = 0; i < allOfSchemas.size(); i++) {
        parsedSchemas.add(parser.parse(allOfSchemas.getValue(i), basePointer.copy().append(Integer.toString(i)), validator));
      }
      validator.setSchemas(parsedSchemas);
      return validator;
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for " + getKeyword() + " keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null " + getKeyword() + " keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey(getKeyword());
  }

  abstract BaseCombinatorsValidator instantiate(MutableStateValidator parent);
  abstract String getKeyword();
}
