package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

public abstract class BaseSingleSchemaValidatorFactory implements ValidatorFactory {
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Object itemsSchema = schema.getValue(getKeyword());
      BaseSingleSchemaValidator validator = instantiate(parent);
      validator.setSchema(parser.parse(itemsSchema, scope.append(getKeyword()), validator));
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

  protected abstract BaseSingleSchemaValidator instantiate(MutableStateValidator parent);
  protected abstract String getKeyword();
}
