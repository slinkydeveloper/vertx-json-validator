package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerImpl;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.openapi3.OpenAPI3SchemaParser;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaRouterTest {

  private JsonObject loadJson(URI uri) throws IOException {
    return new JsonObject(String.join("", Files.readAllLines(Paths.get(uri))));
  }

  private URI buildBaseUri(String filename) {
    return Paths.get("src", "test", "resources", "id_test", filename).toAbsolutePath().toUri();
  }

  private void assertThatSchemaContainsXid(SchemaRouter router, JsonPointer jp, JsonPointerList scope, String id) {
    assertThat(router.resolveCachedSchema(jp, scope)).isNotNull().matches(
        s -> id.equals(((BaseSchema) s).getSchema().getString("x-id")),
        "x-id should match " + id
    );
  }

  @Test
  public void testNoIdKeyword() throws Exception {
    URI baseURI = buildBaseUri("no_id_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouter schemaRouter = new SchemaRouterImpl();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    BaseSchema baseSchema = (BaseSchema) schemaParser.parse();

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create(), baseSchema.getScope(), "main");
    assertThatSchemaContainsXid(schemaRouter, basePointer, baseSchema.getScope(), "main");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("allOf").append("0"), baseSchema.getScope(), "allOf_0");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("allOf").append("1"), baseSchema.getScope(), "allOf_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("anyOf").append("0"), baseSchema.getScope(), "anyOf_0");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("anyOf").append("1"), baseSchema.getScope(), "anyOf_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("oneOf").append("0"), baseSchema.getScope(), "oneOf_0");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("oneOf").append("1"), baseSchema.getScope(), "oneOf_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("not"), baseSchema.getScope(), "not");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("prop_1"), baseSchema.getScope(), "prop_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("prop_2"), baseSchema.getScope(), "prop_2");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("patternProperties").append("^a"), baseSchema.getScope(), "pattern_prop_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("additionalProperties"), baseSchema.getScope(), "additional_prop");
  }

  @Test
  public void testIdURNKeywordFromBaseScope() throws Exception {
    URI baseURI = buildBaseUri("id_urn_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouter schemaRouter = new SchemaRouterImpl();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    BaseSchema baseSchema = (BaseSchema) schemaParser.parse();

    assertThatSchemaContainsXid(schemaRouter, basePointer.copy().append("properties").append("prop_1"), baseSchema.getScope(), "prop_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:590e34ae-8e3d-4bdf-a748-beff72654d0e")), baseSchema.getScope(), "prop_1");
    assertThatSchemaContainsXid(schemaRouter, basePointer.copy().append("properties").append("prop_2"), baseSchema.getScope(), "prop_2");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")), baseSchema.getScope(), "prop_2");
    assertThatSchemaContainsXid(schemaRouter, basePointer.copy().append("properties").append("prop_2").append("not"), baseSchema.getScope(), "not");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")).append("not"), baseSchema.getScope(), "not");
  }

  @Test
  public void testIdURNKeywordFromInnerScope() throws Exception {
    URI baseURI = buildBaseUri("id_urn_keyword.json");
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouter schemaRouter = new SchemaRouterImpl();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    BaseSchema baseSchema = (BaseSchema) schemaParser.parse();
    JsonPointerList scope = ((BaseSchema) schemaRouter.resolveCachedSchema(JsonPointer.fromURI(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")), baseSchema.getScope())).getScope();

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("not"), scope, "not");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")).append("not"), scope, "not");
  }

    /*

   # (document root)
         http://example.com/root.json
         http://example.com/root.json#

   #/definitions/A
         http://example.com/root.json#foo
         http://example.com/root.json#/definitions/A

   #/definitions/B
         http://example.com/other.json
         http://example.com/other.json#
         http://example.com/root.json#/definitions/B

   #/definitions/B/definitions/X
         http://example.com/other.json#bar
         http://example.com/other.json#/definitions/X
         http://example.com/root.json#/definitions/B/definitions/X

   #/definitions/B/definitions/Y
         http://example.com/t/inner.json
         http://example.com/t/inner.json#
         http://example.com/other.json#/definitions/Y
         http://example.com/root.json#/definitions/B/definitions/Y

   #/definitions/C
         urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f
         urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f#
         http://example.com/root.json#/definitions/C
     */

  @Test
  public void testRFCIDKeywordFromBaseScope() throws Exception {
    URI baseURI = buildBaseUri("rfc_id_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouter schemaRouter = new SchemaRouterImpl();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    JsonPointerList scope = ((BaseSchema) schemaParser.parse()).getScope();

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create(), scope, "main");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("A"), scope, "A");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#foo")), scope, "A");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B"), scope, "B");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/other.json")), scope, "B");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B").append("properties").append("X"), scope, "X");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#bar")), scope, "X");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B").append("properties").append("Y"), scope, "Y");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/t/inner.json")), scope, "Y");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/other.json")).append("properties").append("Y"), scope, "Y");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("C"), scope, "C");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/root.json")).append("properties").append("C"), scope, "C");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f")), scope, "C");

  }

  @Test
  public void testRFCIDKeywordFromInnerScope() throws Exception {
    URI baseURI = buildBaseUri("rfc_id_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouter schemaRouter = new SchemaRouterImpl();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    BaseSchema baseSchema = (BaseSchema) schemaParser.parse();
    JsonPointerList scope = ((BaseSchema) schemaRouter.resolveCachedSchema(JsonPointer.fromURI(URI.create("http://example.com/other.json")), baseSchema.getScope())).getScope();

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#foo")), scope, "A");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#bar")), scope, "X");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("Y"), scope, "Y");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B").append("properties").append("Y"), scope, "Y");
  }

}
