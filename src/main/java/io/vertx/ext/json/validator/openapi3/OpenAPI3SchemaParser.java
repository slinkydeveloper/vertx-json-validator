package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaParserOptions;
import io.vertx.ext.json.validator.SchemaRouter;
import io.vertx.ext.json.validator.ValidatorFactory;
import io.vertx.ext.json.validator.generic.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenAPI3SchemaParser extends BaseSchemaParser {

  protected OpenAPI3SchemaParser(SchemaParserOptions options, SchemaRouter router) {
    super(options, router);
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

  /**
   * Instantiate an OpenAPI3SchemaParser
   *
   * @param options
   * @param router
   * @return a new instance of OpenAPI3SchemaParser
   */
  public static OpenAPI3SchemaParser create(SchemaParserOptions options, SchemaRouter router) {
    return new OpenAPI3SchemaParser(options, router);
  }

  /**
   * Parse a openapi 3 schema
   *
   * @param vertx this vertx instance
   * @param schema parsed json schema
   * @param scope scope of json schema
   * @return a new instance of Draft7SchemaParser
   * @throws io.vertx.ext.json.validator.SchemaException if schema is invalid
   */
  public static Schema parse(Vertx vertx, JsonObject schema, URI scope) {
    return new OpenAPI3SchemaParser(new SchemaParserOptions(), SchemaRouter.create(vertx)).parse(schema, scope, null);
  }
}
