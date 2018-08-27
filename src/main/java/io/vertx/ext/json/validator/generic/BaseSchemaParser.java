package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseSchemaParser implements SchemaParser {

    protected final JsonObject schemaRoot;
    protected final URI scope;
    protected final SchemaParserOptions options;
    protected final Map<String, ValidatorFactory> validatorFactoryMap;
    protected final Set<String> keywordsToIgnore;
    protected final SchemaRouter router;

    protected BaseSchemaParser(JsonObject schemaRoot, URI scope, SchemaParserOptions options, SchemaRouter router) {
        this.schemaRoot = schemaRoot;
        this.scope = scope;
        this.options = options;
        this.router = router;
        this.keywordsToIgnore = initKeywordsToIgnore();
        this.validatorFactoryMap = initValidatorFactoryMap();
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

    protected Schema parse(JsonObject json, URI scope) {
        ConcurrentSkipListSet<Validator> validators = new ConcurrentSkipListSet<>();
        URI parsedRelativeId = null;
        if (json.containsKey("$id")) parsedRelativeId = URI.create(json.getString("$id"));

        Set<String> keywords = new HashSet<>(json.getMap().keySet());
        keywords.removeAll(this.keywordsToIgnore);
        for (String keyword : keywords) {
            ValidatorFactory f = validatorFactoryMap.get(keyword);
            if (f != null) {
                Validator v = f.createValidator(json, scope, this);
                if (v != null) validators.add(v);
            }
        }

        Schema s = createSchema(json, validators);
        router.addSchema(s, scope, parsedRelativeId);
        return s;
    }

    protected abstract Schema createSchema(JsonObject schema, ConcurrentSkipListSet<Validator> validators);

    protected abstract Map<String, ValidatorFactory> initValidatorFactoryMap();

    protected abstract Set<String> initKeywordsToIgnore();

    protected void loadOptions() {
        // Load additional validators
        this.validatorFactoryMap.putAll(this.options.getAdditionalValidatorFactories());

        // Load additional string formats
        ValidatorFactory f = validatorFactoryMap.get("format");
        if (f == null) throw new IllegalStateException("This json schema version doesn't support format keyword");
        this.options.getAdditionalStringFormatValidators().forEach(((BaseFormatValidatorFactory)f)::addStringFormatValidator);
    }
}
