package io.vertx.ext.json.validator.generic;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerImpl;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.SchemaParserOptions;
import io.vertx.ext.json.validator.SchemaRouterMock;
import io.vertx.ext.json.validator.openapi3.OpenAPI3SchemaParser;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class SchemaURICreationTest {

  private JsonObject loadJson(URI uri) throws IOException {
    return new JsonObject(String.join("", Files.readAllLines(Paths.get(uri))));
  }

  private URI buildBaseUri(String filename) {
    return Paths.get("src", "test", "resources", "id_test", filename).toAbsolutePath().toUri();
  }

  private void assertThatSchemaContainsXid(Map<JsonPointer, Schema> loadedSchema, JsonPointer jp, String id) {
    assertThat(loadedSchema).containsKey(jp);
    assertThat(((BaseSchema) loadedSchema.get(jp)).getSchema().getString("x-id")).isEqualTo(id);
  }

  @Test
  public void testNoIdKeyword() throws Exception {
    URI baseURI = buildBaseUri("no_id_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouterMock schemaRouter = new SchemaRouterMock();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    BaseSchema baseSchema = (BaseSchema) schemaParser.parse();

    assertThat(baseSchema.getSchema().getString("x-id")).isEqualTo("main");
    Map<JsonPointer, Schema> loadedSchema = schemaRouter.getSchemas();

    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("allOf").append("0"), "allOf_0");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("allOf").append("1"), "allOf_1");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("anyOf").append("0"), "anyOf_0");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("anyOf").append("1"), "anyOf_1");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("oneOf").append("0"), "oneOf_0");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("oneOf").append("1"), "oneOf_1");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("not"), "not");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("prop_1"), "prop_1");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("prop_2"), "prop_2");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("patternProperties").append("^a"), "pattern_prop_1");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("additionalProperties"), "additional_prop");

  }

  @Test
  public void testIdURNKeyword() throws Exception {
    URI baseURI = buildBaseUri("id_urn_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouterMock schemaRouter = new SchemaRouterMock();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    BaseSchema baseSchema = (BaseSchema) schemaParser.parse();

    assertThat(baseSchema.getSchema().getString("x-id")).isEqualTo("main");
    Map<JsonPointer, Schema> loadedSchema = schemaRouter.getSchemas();

    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("prop_1"), "prop_1");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("urn:uuid:590e34ae-8e3d-4bdf-a748-beff72654d0e")), "prop_1");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("urn:uuid:590e34ae-8e3d-4bdf-a748-beff72654d0e#")), "prop_1");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("prop_2"), "prop_2");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")), "prop_2");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672#")), "prop_2");
    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("prop_2").append("not"), "not");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("urn:uuid:77ed19ca-1127-42dd-8194-3e48661ce672")).append("not"), "not");

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
  public void testRFCIDKeyword() throws Exception {
    URI baseURI = buildBaseUri("rfc_id_keyword.json");
    JsonPointer basePointer = new JsonPointerImpl(baseURI);
    JsonObject baseSchemaJson = loadJson(baseURI);
    SchemaRouterMock schemaRouter = new SchemaRouterMock();
    SchemaParser schemaParser = OpenAPI3SchemaParser.create(baseSchemaJson, baseURI, new SchemaParserOptions(), schemaRouter);
    BaseSchema baseSchema = (BaseSchema) schemaParser.parse();

    assertThat(baseSchema.getSchema().getString("x-id")).isEqualTo("main");
    Map<JsonPointer, Schema> loadedSchema = schemaRouter.getSchemas();

    assertThatSchemaContainsXid(loadedSchema, basePointer, "main");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json")), "main");

    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("A"), "A");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json")).append("properties").append("A"), "A");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json#foo")), "A");

    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("B"), "B");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json")).append("properties").append("B"), "B");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/other.json")), "B");

    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("B").append("properties").append("X"), "X");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json")).append("properties").append("B").append("properties").append("X"), "X");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json#bar")), "X"); //TODO ?!
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/other.json#bar")), "X");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/other.json")).append("properties").append("X"), "X");

    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("B").append("properties").append("Y"), "Y");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json")).append("properties").append("B").append("properties").append("Y"), "Y");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/t/inner.json")), "Y");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/other.json")).append("properties").append("Y"), "Y");

    assertThatSchemaContainsXid(loadedSchema, basePointer.copy().append("properties").append("C"), "C");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("http://example.com/root.json")).append("properties").append("C"), "C");
    assertThatSchemaContainsXid(loadedSchema, new JsonPointerImpl(URI.create("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f")), "C");

  }

}
