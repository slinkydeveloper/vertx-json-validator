package io.vertx.ext.json.schema.oas3;

import io.vertx.core.Vertx;
import io.vertx.ext.json.schema.*;
import io.vertx.ext.json.schema.generic.SchemaRouterImpl;
import io.vertx.ext.json.schema.openapi3.OpenAPI3SchemaParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OAS3IntegrationTest extends BaseIntegrationTest {

  @Override
  public Map.Entry<SchemaParser, Schema> buildSchemaFunction(Vertx vertx, Object schema, String testFileName) {
    SchemaParser parser = OpenAPI3SchemaParser.create(new SchemaParserOptions(), new SchemaRouterImpl(vertx.createHttpClient(), vertx.fileSystem(), new SchemaRouterOptions()));
    Schema s = parser.parse(schema, Paths.get(this.getTckPath() + "/" + testFileName + ".json").toAbsolutePath().toUri());
    return new AbstractMap.SimpleImmutableEntry<>(parser, s);
  }

  @Override
  public Stream<String> getTestFiles() {
    return Stream.of(
        "additionalProperties",
        "allOf",
        "anyOf",
//        "discriminator",
        "enum",
        "exclusiveMaximum",
        "exclusiveMinimum",
        "format",
        "items",
        "maximum",
        "maxItems",
        "maxLength",
        "maxProperties",
        "minimum",
        "minItems",
        "minLength",
        "minProperties",
        "multipleOf",
        "not",
        "nullable",
        "oneOf",
        "pattern",
        "properties",
        "ref",
        "refRemote",
        "required",
        "type",
        "uniqueItems"
    );
  }

  @Override
  public Path getTckPath() {
    return Paths.get("src", "test", "resources", "tck", "openapi3");
  }

  @Override
  public Path getRemotesPath() {
    return Paths.get("src", "test", "resources", "tck", "openapi3", "remotes");
  }
}
