package io.vertx.ext.json.schema.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;
import io.vertx.ext.json.schema.generic.JsonSchemaType;

public class TypeValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      String type = schema.getString("type");
      String format = schema.getString("format");
      if (type == null) throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null type keyword");
      return new TypeValidator(parseType(type, format, schema));
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for type/format/nullable keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
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

    public TypeValidator(JsonSchemaType type) {
      this.type = type;
    }

    @Override
    public ValidatorPriority getPriority() {
      return ValidatorPriority.MAX_PRIORITY;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value != null) {
        if (!type.checkInstance(value)) throw ValidationErrorType.NO_MATCH.createException("input don't match type " + type.name(), "type", value);
      }
    }
  }
}
