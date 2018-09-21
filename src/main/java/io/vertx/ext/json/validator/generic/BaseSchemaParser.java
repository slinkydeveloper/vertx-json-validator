package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseSchemaParser implements SchemaParser {

  protected final static Schema TRUE_SCHEMA = (in) -> Future.succeededFuture();
  protected final static Schema FALSE_SCHEMA = (in) -> Future.failedFuture(ValidationErrorType.NO_MATCH.createException("False schema always fail validation", null, in));

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
  public Schema parse(Object jsonSchema, JsonPointer scope) {
    if (!scope.getURIWithoutFragment().isAbsolute()) throw new IllegalArgumentException("The scope provided must be absolute!");
    if (jsonSchema instanceof JsonObject) {
      JsonObject json = (JsonObject) jsonSchema;
      ConcurrentSkipListSet<Validator> validators = new ConcurrentSkipListSet<>(ValidatorPriority.VALIDATOR_COMPARATOR);

      Schema s = createSchema(json, scope, validators);
      router.addSchema(s, scope);

      for (ValidatorFactory factory : validatorFactories) {
        if (factory.canConsumeSchema(json)) {
          Validator v = factory.createValidator(json, scope.copy(), this);
          if (v != null) validators.add(v);
        }
      }

      return s;
    } else if (jsonSchema instanceof Boolean) {
      Schema s = ((Boolean) jsonSchema) ? TRUE_SCHEMA : FALSE_SCHEMA;
      router.addSchema(s, scope);
      return s;
    } else
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(jsonSchema, "Schema should be a JsonObject or a Boolean");
  }

  protected Schema createSchema(JsonObject schema, JsonPointer scope, ConcurrentSkipListSet<Validator> validators) {
    if (schema.containsKey("$ref")) return new RefSchema(schema, scope, validators, this);
    else return new SchemaImpl(schema, scope, validators);
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
  public Schema parseSchemaFromString(String unparsedJson, JsonPointer scope) {
    String unparsedSchema = unparsedJson.trim();
    if ("false".equals(unparsedSchema) || "true".equals(unparsedSchema))
      return this.parse(Boolean.parseBoolean(unparsedSchema), scope);
    else return this.parse(new JsonObject(unparsedSchema), scope);
  }
}
