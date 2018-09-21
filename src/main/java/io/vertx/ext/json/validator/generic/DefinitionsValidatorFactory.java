package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.SchemaErrorType;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.Validator;
import io.vertx.ext.json.validator.ValidatorFactory;

import java.util.Map;

public class DefinitionsValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonObject definitions = schema.getJsonObject("definitions");
      JsonPointer basePointer = scope.append("definitions");
      definitions.forEach(e -> {
        parser.parse((e.getValue() instanceof Map) ? new JsonObject((Map<String, Object>) e.getValue()) : e.getValue(), basePointer.copy().append(e.getKey()));
      });
      return null;
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for definitions keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null definitions keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("definitions");
  }

}
