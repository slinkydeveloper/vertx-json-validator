package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.JsonSchemaType;

public class TypeValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      String type = schema.getString("type");
      String format = schema.getString("format");
      Boolean nullable = schema.getBoolean("nullable");
      if (type == null) throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null type keyword");
      return new TypeValidator(parseType(type, format, schema), (nullable == null) ? false : nullable);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for type/format/nullable keyword");
    }
  }

  @Override
  public boolean canCreateValidator(JsonObject schema) {
    return schema.containsKey("type");
  }

  private static JsonSchemaType parseType(String type, String format, JsonObject schema) {
    switch (type) {
      case "integer":
        return JsonSchemaType.INTEGER;
      case "number":
        return (format != null && (format.equals("double") || format.equals("float"))) ? JsonSchemaType.NUMBER_DECIMAL : JsonSchemaType.NUMBER;
      case "boolean":
        return JsonSchemaType.BOOLEAN;
      case "string":
        return JsonSchemaType.STRING;
      case "object":
        return JsonSchemaType.OBJECT;
      case "array":
        return JsonSchemaType.ARRAY;
      default:
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Unknown type: " + type);
    }
  }

  class TypeValidator implements SyncValidator {

    final JsonSchemaType type;
    final boolean nullable;

    public TypeValidator(JsonSchemaType type, boolean nullable) {
      this.type = type;
      this.nullable = nullable;
    }

    @Override
    public ValidatorPriority getPriority() {
      return ValidatorPriority.MAX_PRIORITY;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value == null) {
        if (!nullable) throw ValidationExceptionFactory.generateNotMatchValidationException(""); //TODO
      } else {
        if (!type.checkInstance(value)) throw ValidationExceptionFactory.generateNotMatchValidationException("");
      }
    }
  }
}
