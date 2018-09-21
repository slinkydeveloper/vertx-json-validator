package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class PropertiesValidatorFactory implements ValidatorFactory {

  private Schema parseAdditionalProperties(Object obj, JsonPointer scope, SchemaParser parser) {
    try {
      return parser.parse(obj, scope.copy().append("additionalProperties"));
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(obj, "Wrong type for additionalProperties keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(obj, "Null additionalProperties keyword");
    }
  }

  private Map<String, Schema> parseProperties(JsonObject obj, JsonPointer scope, SchemaParser parser) {
    JsonPointer basePointer = scope.copy().append("properties");
    Map<String, Schema> parsedSchemas = new HashMap<>();
    for (Map.Entry<String, Object> entry : obj) {
      try {
        parsedSchemas.put(entry.getKey(), parser.parse(
            entry.getValue(),
            basePointer.copy().append(entry.getKey())
        ));
      } catch (ClassCastException | NullPointerException e) {
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(obj, "Property descriptor " + entry.getKey() + " should be a not null JsonObject");
      }
    }
    return parsedSchemas;
  }

  private Map<Pattern, Schema> parsePatternProperties(JsonObject obj, JsonPointer scope, SchemaParser parser) {
    JsonPointer basePointer = scope.copy().append("patternProperties");
    Map<Pattern, Schema> parsedSchemas = new HashMap<>();
    for (Map.Entry<String, Object> entry : obj) {
      try {
        parsedSchemas.put(Pattern.compile(entry.getKey()), parser.parse(
            entry.getValue(),
            basePointer.copy().append(entry.getKey())
        ));
      } catch (PatternSyntaxException e) {
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(obj, "Invalid pattern for pattern keyword");
      } catch (ClassCastException | NullPointerException e) {
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(obj, "Property descriptor " + entry.getKey() + " should be a not null JsonObject");
      }
    }
    return parsedSchemas;
  }

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonObject properties = schema.getJsonObject("properties");
      JsonObject patternProperties = schema.getJsonObject("patternProperties");
      Object additionalProperties = schema.getValue("additionalProperties");

      Map<String, Schema> parsedProperties = (properties != null) ? parseProperties(properties, scope, parser) : null;
      Map<Pattern, Schema> parsedPatternProperties = (patternProperties != null) ? parsePatternProperties(patternProperties, scope, parser) : null;

      if (additionalProperties instanceof JsonObject) {
        return new PropertiesValidator(parsedProperties, parsedPatternProperties, parseAdditionalProperties(additionalProperties, scope, parser));
      } else if (additionalProperties instanceof Boolean) {
        return new PropertiesValidator(parsedProperties, parsedPatternProperties, (Boolean) additionalProperties);
      } else {
        return new PropertiesValidator(parsedProperties, parsedPatternProperties, true);
      }
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for properties/patternProperties keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("properties") || schema.containsKey("patternProperties") || schema.containsKey("additionalProperties");
  }

  class PropertiesValidator implements AsyncValidator {

    private Map<String, Schema> properties;
    private Map<Pattern, Schema> patternProperties;
    private boolean allowAdditionalProperties;
    private Schema additionalPropertiesSchema;

    public PropertiesValidator(Map<String, Schema> properties, Map<Pattern, Schema> patternProperties, boolean allowAdditionalProperties) {
      this.properties = properties;
      this.patternProperties = patternProperties;
      this.allowAdditionalProperties = allowAdditionalProperties;
      this.additionalPropertiesSchema = null;
    }

    public PropertiesValidator(Map<String, Schema> properties, Map<Pattern, Schema> patternProperties, Schema additionalPropertiesSchema) {
      this.properties = properties;
      this.patternProperties = patternProperties;
      this.allowAdditionalProperties = true;
      this.additionalPropertiesSchema = additionalPropertiesSchema;
    }

    private Schema searchPropSchema(String key) {
      if (properties != null && properties.containsKey(key)) return properties.get(key);
      else {
        if (patternProperties != null)
          for (Map.Entry<Pattern, Schema> patternProperty : patternProperties.entrySet()) {
            if (patternProperty.getKey().matcher(key).lookingAt()) return patternProperty.getValue();
          }
        return null;
      }
    }

    @Override
    public Future validate(Object in) {
      if (in instanceof JsonObject) {
        JsonObject obj = (JsonObject) in;
        List<Future> futs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : obj) {
          Schema s = searchPropSchema(entry.getKey());
          if (s != null) futs.add(s.validate(entry.getValue()));
          else {
            if (allowAdditionalProperties) {
              if (additionalPropertiesSchema != null) futs.add(additionalPropertiesSchema.validate(entry.getValue()).recover(t -> Future.failedFuture(NO_MATCH.createException("additionalProperties schema should match", (Throwable) t, "additionalProperties", in))));
            } else {
              return Future.failedFuture(NO_MATCH.createException("provided object should not contain additional properties", "additionalProperties", in));
            }
          }
        }
        return CompositeFuture.all(futs).compose(cf -> Future.succeededFuture());
      } else return Future.succeededFuture();
    }
  }

}
