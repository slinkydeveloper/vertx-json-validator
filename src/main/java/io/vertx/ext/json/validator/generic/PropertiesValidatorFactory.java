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
import java.util.stream.Stream;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class PropertiesValidatorFactory implements ValidatorFactory {

  private Schema parseAdditionalProperties(Object obj, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      return parser.parse(obj, scope.copy().append("additionalProperties"), parent);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(obj, "Wrong type for additionalProperties keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(obj, "Null additionalProperties keyword");
    }
  }

  private Map<String, Schema> parseProperties(JsonObject obj, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    JsonPointer basePointer = scope.copy().append("properties");
    Map<String, Schema> parsedSchemas = new HashMap<>();
    for (Map.Entry<String, Object> entry : obj) {
      try {
        parsedSchemas.put(entry.getKey(), parser.parse(
            entry.getValue(),
            basePointer.copy().append(entry.getKey()),
            parent
        ));
      } catch (ClassCastException | NullPointerException e) {
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(obj, "Property descriptor " + entry.getKey() + " should be a not null JsonObject");
      }
    }
    return parsedSchemas;
  }

  private Map<Pattern, Schema> parsePatternProperties(JsonObject obj, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    JsonPointer basePointer = scope.copy().append("patternProperties");
    Map<Pattern, Schema> parsedSchemas = new HashMap<>();
    for (Map.Entry<String, Object> entry : obj) {
      try {
        parsedSchemas.put(Pattern.compile(entry.getKey()), parser.parse(
            entry.getValue(),
            basePointer.copy().append(entry.getKey()),
            parent
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
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      JsonObject properties = schema.getJsonObject("properties");
      JsonObject patternProperties = schema.getJsonObject("patternProperties");
      Object additionalProperties = schema.getValue("additionalProperties");

      PropertiesValidator validator = new PropertiesValidator(parent);

      Map<String, Schema> parsedProperties = (properties != null) ? parseProperties(properties, scope, parser, validator) : null;
      Map<Pattern, Schema> parsedPatternProperties = (patternProperties != null) ? parsePatternProperties(patternProperties, scope, parser, validator) : null;

      if (additionalProperties instanceof JsonObject) {
        validator.configure(parsedProperties, parsedPatternProperties, parseAdditionalProperties(additionalProperties, scope, parser, validator));
      } else if (additionalProperties instanceof Boolean) {
        validator.configure(parsedProperties, parsedPatternProperties, (Boolean) additionalProperties);
      } else {
        validator.configure(parsedProperties, parsedPatternProperties, true);
      }
      return validator;
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for properties/patternProperties keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("properties") || schema.containsKey("patternProperties") || schema.containsKey("additionalProperties");
  }

  private Future<Void> fillAdditionalPropertyException(Throwable t, Object in) {
    return Future.failedFuture(NO_MATCH.createException("additionalProperties schema should match", t, "additionalProperties", in));
  }

  class PropertiesValidator extends BaseMutableStateValidator implements ValidatorWithDefaultApply {

    private Map<String, Schema> properties;
    private Map<Pattern, Schema> patternProperties;
    private boolean allowAdditionalProperties;
    private Schema additionalPropertiesSchema;

    public PropertiesValidator(MutableStateValidator parent) {
      super(parent);
    }

    private void configure(Map<String, Schema> properties, Map<Pattern, Schema> patternProperties, boolean allowAdditionalProperties) {
      this.properties = properties;
      this.patternProperties = patternProperties;
      this.allowAdditionalProperties = allowAdditionalProperties;
      this.additionalPropertiesSchema = null;
      initializeIsSync();
    }

    private void configure(Map<String, Schema> properties, Map<Pattern, Schema> patternProperties, Schema additionalPropertiesSchema) {
      this.properties = properties;
      this.patternProperties = patternProperties;
      this.allowAdditionalProperties = true;
      this.additionalPropertiesSchema = additionalPropertiesSchema;
      initializeIsSync();
    }

    @Override
    public boolean calculateIsSync() {
      Stream<Boolean> props = (properties != null) ? properties.values().stream().map(Schema::isSync) : Stream.empty();
      Stream<Boolean> patternProps = (patternProperties != null) ? patternProperties.values().stream().map(Schema::isSync) : Stream.empty();
      Stream<Boolean> additionalProps = (additionalPropertiesSchema != null) ? Stream.of(additionalPropertiesSchema.isSync()) : Stream.empty();
      return Stream.concat(
          props,
          Stream.concat(patternProps, additionalProps)
      ).reduce(true, Boolean::logicalAnd);
    }

    @Override
    public Future<Void> validateAsync(Object in) {
      if (isSync()) return validateSyncAsAsync(in);
      if (in instanceof JsonObject) {
        JsonObject obj = (JsonObject) in;
        List<Future> futs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : obj) {
          boolean found = false;
          String key = entry.getKey();
          if (properties != null && properties.containsKey(key)) {
            Schema s = properties.get(key);
            if (s.isSync()) {
              try {
                s.validateSync(entry.getValue());
              } catch (ValidationException e) {
                return Future.failedFuture(e);
              }
            } else {
              futs.add(s.validateAsync(entry.getValue()));
            }
            found = true;
          }
          if (patternProperties != null) {
            for (Map.Entry<Pattern, Schema> patternProperty : patternProperties.entrySet()) {
              if (patternProperty.getKey().matcher(key).find()) {
                Schema s = patternProperty.getValue();
                if (s.isSync()) {
                  try {
                    s.validateSync(entry.getValue());
                  } catch (ValidationException e) {
                    return Future.failedFuture(e);
                  }
                } else {
                  futs.add(s.validateAsync(entry.getValue()));
                }
                found = true;
              }
            }
          }
          if (!found) {
            if (allowAdditionalProperties) {
              if (additionalPropertiesSchema != null) {
                if (additionalPropertiesSchema.isSync()) {
                  try {
                    additionalPropertiesSchema.validateSync(entry.getValue());
                  } catch (ValidationException e) {
                    return fillAdditionalPropertyException(e, in);
                  }
                } else {
                  futs.add(additionalPropertiesSchema.validateAsync(entry.getValue()).recover(t -> fillAdditionalPropertyException(t, in)));
                }
              }
            } else {
              return Future.failedFuture(NO_MATCH.createException("provided object should not contain additional properties", "additionalProperties", in));
            }
          }
        }
        if (futs.isEmpty()) return Future.succeededFuture();
        else return CompositeFuture.all(futs).compose(cf -> Future.succeededFuture());
      } else return Future.succeededFuture();
    }

    @Override
    public void validateSync(Object in) throws ValidationException {
      this.checkSync();
      if (in instanceof JsonObject) {
        JsonObject obj = (JsonObject) in;
        for (Map.Entry<String, Object> entry : obj) {
          boolean found = false;
          String key = entry.getKey();
          if (properties != null && properties.containsKey(key)) {
            Schema s = properties.get(key);
            s.validateSync(entry.getValue());
            found = true;
          }
          if (patternProperties != null) {
            for (Map.Entry<Pattern, Schema> patternProperty : patternProperties.entrySet()) {
              if (patternProperty.getKey().matcher(key).find()) {
                Schema s = patternProperty.getValue();
                s.validateSync(entry.getValue());
                found = true;
              }
            }
          }
          if (!found) {
            if (allowAdditionalProperties) {
              if (additionalPropertiesSchema != null) {
                additionalPropertiesSchema.validateSync(entry.getValue());
              }
            } else {
              throw NO_MATCH.createException("provided object should not contain additional properties", "additionalProperties", in);
            }
          }
        }
      }
    }

    @Override
    public void applyDefaultValue(Object value) {
      if (value instanceof JsonObject) {
        JsonObject obj = (JsonObject) value;
        for (Map.Entry<String, Schema> e : properties.entrySet()) {
          if (!obj.containsKey(e.getKey()) && e.getValue().hasDefaultValue()) {
            obj.put(e.getKey(), e.getValue().getDefaultValue());
          } else if (obj.containsKey(e.getKey())) {
            e.getValue().applyDefaultValues(obj.getValue(e.getKey()));
          }
        }
      }
    }
  }

}
