package io.vertx.ext.json.validator.draft7;

import io.vertx.core.Vertx;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.SchemaRouterImpl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class Draft7IntegrationTest extends BaseIntegrationTest {

  @Override
  public Map.Entry<SchemaParser, Schema> buildSchemaFunction(Vertx vertx, Object schema, String testFileName) {
    SchemaParser parser = Draft7SchemaParser.create(new SchemaParserOptions(), new SchemaRouterImpl(vertx.createHttpClient(), vertx.fileSystem(), new SchemaRouterOptions()));
    Schema s = parser.parse(schema, Paths.get(this.getTckPath() + "/" + testFileName + ".json").toAbsolutePath().toUri());
    return new AbstractMap.SimpleImmutableEntry<>(parser, s);
  }

  @Override
  public Stream<String> getTestFiles() {
    return Stream.of(
        "additionalItems",
        "additionalProperties",
        "allOf",
        "anyOf",
        "boolean_schema",
        "const",
        "contains",
        "definitions",
        "dependencies",
        "enum",
        "exclusiveMaximum",
        "exclusiveMinimum",
        "if-then-else",
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
        "oneOf",
        "pattern",
        "patternProperties",
        "properties",
        "propertyNames",
        "ref",
        "refRemote",
        "required",
        "type",
        "uniqueItems"
    );
  }

  @Override
  public Path getTckPath() {
    return Paths.get("src",  "test", "resources",  "tck", "draft7");
  }

  @Override
  public Path getRemotesPath() {
    return Paths.get("src", "test", "resources", "tck", "draft7", "remotes");
  }
}
