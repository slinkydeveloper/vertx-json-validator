package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseSchemaParser implements SchemaParser {

    private final static Schema TRUE_SCHEMA = (in) -> Future.succeededFuture();
    private final static Schema FALSE_SCHEMA = (in) -> Future.failedFuture(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO

    protected final Object schemaRoot;
    protected final URI scope;
    protected final SchemaParserOptions options;
    protected final List<ValidatorFactory> validatorFactories;
    protected final SchemaRouter router;

    protected BaseSchemaParser(Object schemaRoot, URI scope, SchemaParserOptions options, SchemaRouter router) {
        this.schemaRoot = schemaRoot;
        this.scope = scope;
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
    public Schema parse() {
        return this.parse(schemaRoot, scope);
    }

    public Schema parse(Object schema, URI scope) {
        if (schema instanceof JsonObject) {
            JsonObject json = (JsonObject)schema;
            ConcurrentSkipListSet<Validator> validators = new ConcurrentSkipListSet<>(ValidatorPriority.VALIDATOR_COMPARATOR);
            URI parsedRelativeId = null;
            if (json.containsKey("$id")) parsedRelativeId = URI.create(json.getString("$id"));

            for (ValidatorFactory factory : validatorFactories) {
                if (factory.canCreateValidator(json)) {
                    Validator v = factory.createValidator(json, scope, this);
                    if (v != null) validators.add(v);
                }
            }

            Schema s = createSchema(json, validators);
            router.addSchema(s, scope, parsedRelativeId);
            return s;
        } else if (schema instanceof Boolean) {
            Schema s = ((Boolean)schema) ? TRUE_SCHEMA : FALSE_SCHEMA;
            router.addSchema(s, scope, null);
            return s;
        } else throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Schema should be a JsonObject or a Boolean");
    }

    protected abstract Schema createSchema(Object schema, ConcurrentSkipListSet<Validator> validators);

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
        this.options.getAdditionalStringFormatValidators().forEach(((BaseFormatValidatorFactory)f)::addStringFormatValidator);
    }
}
