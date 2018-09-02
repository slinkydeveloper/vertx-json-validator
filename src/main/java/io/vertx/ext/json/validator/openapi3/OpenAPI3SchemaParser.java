package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class OpenAPI3SchemaParser extends BaseSchemaParser {
    protected OpenAPI3SchemaParser(Object schemaRoot, URI scope, SchemaParserOptions options, SchemaRouter router) {
        super(schemaRoot, scope, options, router);
    }

    @Override
    protected Schema createSchema(Object schema, ConcurrentSkipListSet<Validator> validators) {
        return new OpenAPI3Schema(schema, validators);
    }

    @Override
    protected List<ValidatorFactory> initValidatorFactories() {
        List<ValidatorFactory> factories = new ArrayList<>();
        factories.add(new FormatValidatorFactory());
        factories.add(new MaximumValidatorFactory());
        factories.add(new MinimumValidatorFactory());
        factories.add(new NullableValidatorFactory());
        factories.add(new TypeValidatorFactory());
        factories.add(new AllOfValidatorFactory());
        factories.add(new AnyOfValidatorFactory());
        factories.add(new EnumValidatorFactory());
        factories.add(new ItemsValidatorFactory());
        factories.add(new MaxItemsValidatorFactory());
        factories.add(new MaxLengthValidatorFactory());
        factories.add(new MaxPropertiesValidatorFactory());
        factories.add(new MinItemsValidatorFactory());
        factories.add(new MinLengthValidatorFactory());
        factories.add(new MinPropertiesValidatorFactory());
        factories.add(new MultipleOfValidatorFactory());
        factories.add(new NotValidatorFactory());
        factories.add(new OneOfValidatorFactory());
        factories.add(new PatternValidatorFactory());
        factories.add(new PropertiesValidatorFactory());
        factories.add(new RequiredValidatorFactory());
        factories.add(new UniqueItemsValidatorFactory());
        return factories;
    }

    public static OpenAPI3SchemaParser create(Object schemaRoot, URI scope, SchemaParserOptions options, SchemaRouter router) {
        return new OpenAPI3SchemaParser(schemaRoot, scope, options, router);
    }
}
