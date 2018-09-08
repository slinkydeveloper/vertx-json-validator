package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.file.FileSystem;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.*;
import io.vertx.ext.web.client.WebClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class OpenAPI3SchemaParser extends BaseSchemaParser {

  public OpenAPI3SchemaParser(Object schemaRoot, URI baseScope, SchemaParserOptions options, SchemaRouter router, WebClient client, FileSystem fs) {
    super(schemaRoot, baseScope, options, router, client, fs);
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
    factories.add(new DefinitionsValidatorFactory());
    return factories;
  }

  public static OpenAPI3SchemaParser create(Object schemaRoot, URI scope, SchemaParserOptions options, SchemaRouter router, WebClient client, FileSystem fs) {
    return new OpenAPI3SchemaParser(schemaRoot, scope, options, router, client, fs);
  }
}
