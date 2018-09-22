package io.vertx.ext.json.validator.generic;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerImpl;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.SchemaParserOptions;
import io.vertx.ext.json.validator.SchemaRouter;
import io.vertx.ext.json.validator.openapi3.OpenAPI3SchemaParser;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaRouterTest {

  public Vertx vertx;
  public SchemaParser parser;
  public SchemaRouter schemaRouter;

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    schemaRouter = SchemaRouter.create(vertx);
    parser = OpenAPI3SchemaParser.create(new SchemaParserOptions(), schemaRouter);
  }

  private JsonObject loadJson(URI uri) throws IOException {
    return new JsonObject(String.join("", Files.readAllLines(Paths.get(uri))));
  }

  private URI buildBaseUri(String filename) {
    return Paths.get("src", "test", "resources", "id_test", filename).toAbsolutePath().toUri();
  }

  private void assertThatSchemaContainsXid(SchemaRouter router, JsonPointer jp, JsonPointer scope, String id) {
    assertThat(router.resolveCachedSchema(jp, scope, parser)).isNotNull().matches(
        s -> id.equals(((SchemaImpl) s).getSchema().getString("x-id")),
        "x-id should match " + id
    );
  }

  @Test
  public void testNoIdKeyword() throws Exception {
    URI baseURI = buildBaseUri("no_id_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    parser.parse(baseSchemaJson, baseURI);

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create(), basePointer, "main");
    assertThatSchemaContainsXid(schemaRouter, basePointer, basePointer, "main");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("allOf").append("0"), basePointer, "allOf_0");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("allOf").append("1"), basePointer, "allOf_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("anyOf").append("0"), basePointer, "anyOf_0");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("anyOf").append("1"), basePointer, "anyOf_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("oneOf").append("0"), basePointer, "oneOf_0");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("oneOf").append("1"), basePointer, "oneOf_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("not"), basePointer, "not");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("prop_1"), basePointer, "prop_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("prop_2"), basePointer, "prop_2");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("patternProperties").append("^a"), basePointer, "pattern_prop_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("additionalProperties"), basePointer, "additional_prop");
  }

  @Test
  public void testIdURNKeywordFromBaseScope() throws Exception {
    URI baseURI = buildBaseUri("id_urn_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    parser.parse(baseSchemaJson, baseURI);

    assertThatSchemaContainsXid(schemaRouter, basePointer.copy().append("properties").append("prop_1"), basePointer, "prop_1");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:590e34ae-8e3d-4bdf-a748-beff72654d0e")), basePointer, "prop_1");
    assertThatSchemaContainsXid(schemaRouter, basePointer.copy().append("properties").append("prop_2"), basePointer, "prop_2");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")), basePointer, "prop_2");
    assertThatSchemaContainsXid(schemaRouter, basePointer.copy().append("properties").append("prop_2").append("not"), basePointer, "not");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")).append("not"), basePointer, "not");
  }

  @Test
  public void testIdURNKeywordFromInnerScope() throws Exception {
    URI baseURI = buildBaseUri("id_urn_keyword.json");
    JsonObject baseSchemaJson = loadJson(baseURI);
    parser.parse(baseSchemaJson, baseURI);
    JsonPointer scope = schemaRouter.resolveCachedSchema(JsonPointer.fromURI(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")), JsonPointer.fromURI(baseURI), parser).getScope();

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
    parser.parse(baseSchemaJson, baseURI);

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create(), basePointer, "main");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("A"), basePointer, "A");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#foo")), basePointer, "A");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B"), basePointer, "B");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/other.json")), basePointer, "B");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B").append("properties").append("X"), basePointer, "X");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#bar")), basePointer, "X");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B").append("properties").append("Y"), basePointer, "Y");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/t/inner.json")), basePointer, "Y");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/other.json")).append("properties").append("Y"), basePointer, "Y");

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("C"), basePointer, "C");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("http://example.com/root.json")).append("properties").append("C"), basePointer, "C");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f")), basePointer, "C");

  }

  @Test
  public void testRFCIDKeywordFromInnerScope() throws Exception {
    URI baseURI = buildBaseUri("rfc_id_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    parser.parse(baseSchemaJson, baseURI);
    JsonPointer scope = schemaRouter.resolveCachedSchema(JsonPointer.fromURI(URI.create("http://example.com/other.json")), basePointer, parser).getScope();

    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#foo")), scope, "A");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.fromURI(URI.create("#bar")), scope, "X");
    assertThatSchemaContainsXid(schemaRouter, JsonPointer.create().append("properties").append("B").append("properties").append("Y"), scope, "Y");
  }

}
