package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

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
    loadOptions();
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
      ConcurrentSkipListSet<Validator> validators = new ConcurrentSkipListSet<>(ValidatorPriority.VALIDATOR_COMPARATOR);

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
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(jsonSchema, "Schema should be a JsonObject or a Boolean");
  }

  protected SchemaImpl createSchema(JsonObject schema, JsonPointer scope, MutableStateValidator parent) {
    if (schema.containsKey("$ref")) return new RefSchema(schema, scope, this, parent);
    else return new SchemaImpl(schema, scope, parent);
  }

  protected abstract List<ValidatorFactory> initValidatorFactories();

  protected void loadOptions() {
    // Load additional validators
    this.validatorFactories.addAll(this.options.getAdditionalValidatorFactories());

    // Load additional string formats
    ValidatorFactory f = validatorFactories
        .stream()
        .filter(factory -> factory instanceof BaseFormatValidatorFactory)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("This json schema version doesn't support format keyword"));
    this.options.getAdditionalStringFormatValidators().forEach(((BaseFormatValidatorFactory) f)::addStringFormatValidator);
  }

  @Override
  public Schema parseSchemaFromString(String unparsedJson, JsonPointer scope, MutableStateValidator parent) {
    String unparsedSchema = unparsedJson.trim();
    if ("false".equals(unparsedSchema) || "true".equals(unparsedSchema))
      return this.parse(Boolean.parseBoolean(unparsedSchema), scope, parent);
    else return this.parse(new JsonObject(unparsedSchema), scope, parent);
  }
}
