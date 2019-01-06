package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.Map;

public class DefinitionsValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      JsonObject definitions = schema.getJsonObject("definitions");
      JsonPointer basePointer = scope.append("definitions");
      definitions.forEach(e -> {
        parser.parse((e.getValue() instanceof Map) ? new JsonObject((Map<String, Object>) e.getValue()) : e.getValue(), basePointer.copy().append(e.getKey()), parent);
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
