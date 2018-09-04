package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.impl.JsonPointerImpl;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseSchemaParser implements SchemaParser {

    private final static Schema TRUE_SCHEMA = (in) -> Future.succeededFuture();
    private final static Schema FALSE_SCHEMA = (in) -> Future.failedFuture(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO

    protected final Object schemaRoot;
    protected final URI baseScope;
    protected final SchemaParserOptions options;
    protected final List<ValidatorFactory> validatorFactories;
    protected final SchemaRouter router;

    protected BaseSchemaParser(Object schemaRoot, URI baseScope, SchemaParserOptions options, SchemaRouter router) {
        this.schemaRoot = schemaRoot;
        this.baseScope = baseScope;
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
        return this.parse(schemaRoot, new JsonPointerList(Collections.singletonList(new JsonPointerImpl(baseScope))));
    }

    private void appendIdKeyword(JsonPointerList scope, URI idKeyword) {
        if (idKeyword.isAbsolute()) {
            scope.add(new JsonPointerImpl(idKeyword));
        } else if ((idKeyword.getSchemeSpecificPart() == null || idKeyword.getSchemeSpecificPart().isEmpty()) && idKeyword.getFragment() != null){
            scope.addAll(
                    scope.stream()
                            .map(j -> URIUtils.replaceFragment(((JsonPointerImpl)j).getStartingUri(), idKeyword.getFragment()))
                            .map(JsonPointerImpl::new)
                            .collect(Collectors.toList())
            );
        } else if (idKeyword.getPath() != null) {
            scope.addAll(
                    scope.stream()
                            .map(j -> URIUtils.replacePath(((JsonPointerImpl)j).getStartingUri(), idKeyword.getPath()))
                            .map(JsonPointerImpl::new)
                            .collect(Collectors.toList())
            );
        } else {
            throw new IllegalArgumentException("Unrecognized $id keyword");
        }
    }

    public Schema parse(Object schema, JsonPointerList scope) {
        if (schema instanceof JsonObject) {
            JsonObject json = (JsonObject)schema;
            ConcurrentSkipListSet<Validator> validators = new ConcurrentSkipListSet<>(ValidatorPriority.VALIDATOR_COMPARATOR);
            if (json.containsKey("$id")) appendIdKeyword(scope, URI.create(json.getString("$id")));

            for (ValidatorFactory factory : validatorFactories) {
                if (factory.canCreateValidator(json)) {
                    Validator v = factory.createValidator(json, scope.copyList(), this);
                    if (v != null) validators.add(v);
                }
            }

            Schema s = createSchema(json, scope, validators);
            router.addSchema(s, scope);
            return s;
        } else if (schema instanceof Boolean) {
            Schema s = ((Boolean)schema) ? TRUE_SCHEMA : FALSE_SCHEMA;
            router.addSchema(s, scope);
            return s;
        } else throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Schema should be a JsonObject or a Boolean");
    }

    protected abstract Schema createSchema(Object schema, JsonPointerList scope, ConcurrentSkipListSet<Validator> validators);

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
