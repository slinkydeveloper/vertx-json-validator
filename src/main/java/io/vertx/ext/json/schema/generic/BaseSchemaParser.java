package io.vertx.ext.json.schema.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseSchemaParser implements SchemaParser {

  protected final SchemaParserOptions options;
  protected final List<ValidatorFactory> validatorFactories;
  protected final SchemaRouter router;

  protected BaseSchemaParser(SchemaParserOptions options, SchemaRouter router) {
    this.options = options;
    this.router = router;
    this.validatorFactories = initValidatorFactories();
  }

  @Override
  public SchemaRouter getSchemaRouter() {
    return router;
  }

  @Override
  public Schema parse(Object jsonSchema, JsonPointer scope, MutableStateValidator parent) {
    if (!scope.getURIWithoutFragment().isAbsolute()) throw new IllegalArgumentException("The scope provided must be absolute!");
    if (jsonSchema instanceof Map) jsonSchema = new JsonObject((Map<String, Object>) jsonSchema);
    if (jsonSchema instanceof JsonObject) {
      JsonObject json = (JsonObject) jsonSchema;
      Set<Validator> validators = new HashSet<>();

      SchemaImpl s = createSchema(json, scope, parent);
      router.addSchema(s);

      for (ValidatorFactory factory : validatorFactories) {
        if (factory.canConsumeSchema(json)) {
          Validator v = factory.createValidator(json, scope.copy(), this, s);
          if (v != null) validators.add(v);
        }
      }
      s.setValidators(validators);
      return s;
    } else if (jsonSchema instanceof Boolean) {
      Schema s = ((Boolean) jsonSchema) ? TrueSchema.getInstance() : FalseSchema.getInstance();
      router.addSchema(s);
      return s;
    } else
      throw new SchemaException(jsonSchema, "Schema must be a JsonObject or a Boolean");
  }

  protected SchemaImpl createSchema(JsonObject schema, JsonPointer scope, MutableStateValidator parent) {
    if (schema.containsKey("$ref")) return new RefSchema(schema, scope, this, parent);
    else return new SchemaImpl(schema, scope, parent);
  }

  protected abstract List<ValidatorFactory> initValidatorFactories();

  @Override
  public SchemaParser withValidatorFactory(ValidatorFactory factory) {
    this.validatorFactories.add(factory);
    return this;
  }

  @Override
  public SchemaParser withStringFormatValidator(String formatName, Predicate<String> predicate) {
    BaseFormatValidatorFactory f = (BaseFormatValidatorFactory) validatorFactories
        .stream()
        .filter(factory -> factory instanceof BaseFormatValidatorFactory)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("This json schema version doesn't support format keyword"));
    f.addStringFormatValidator(formatName, predicate);
    return this;
  }

  @Override
  public Schema parseSchemaFromString(String unparsedJson, JsonPointer scope, MutableStateValidator parent) {
    String unparsedSchema = unparsedJson.trim();
    if ("false".equals(unparsedSchema) || "true".equals(unparsedSchema))
      return this.parse(Boolean.parseBoolean(unparsedSchema), scope, parent);
    else return this.parse(new JsonObject(unparsedSchema), scope, parent);
  }
}
