package io.vertx.ext.json.validator.generic;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.SchemaParserOptions;
import io.vertx.ext.json.validator.SchemaRouter;
import io.vertx.ext.json.validator.openapi3.OpenAPI3SchemaParser;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class LocalRefTest {

  public Vertx vertx;
  public HttpClient client;
  public SchemaParser parser;
  public SchemaRouter router;

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    client = vertx.createHttpClient();
    router = new SchemaRouterImpl(client, vertx.fileSystem());
  }

  private JsonObject loadJson(URI uri) throws IOException {
    return new JsonObject(String.join("", Files.readAllLines(Paths.get(uri))));
  }

  private Path buildBasePath(String filename) {
    return Paths.get(".","src", "test", "resources", "ref_test", filename);
  }

  private void assertThatSchemaContainsXid(SchemaRouter router, JsonPointer jp, JsonPointer scope, String id) {
    assertThat(router.resolveCachedSchema(jp, scope)).isNotNull().matches(
        s -> id.equals(((SchemaImpl) s).getSchema().getString("x-id")),
        "x-id should match " + id
    );
  }

  @Test
  public void absoluteLocalRef(TestContext context) {
    URI sampleURI = buildBasePath("sample.json").toAbsolutePath().toUri();
    JsonObject mainSchemaUnparsed = new JsonObject().put("$ref", sampleURI.toString());
    parser = OpenAPI3SchemaParser.create(mainSchemaUnparsed, buildBasePath("test_1.json").toAbsolutePath().toUri(), new SchemaParserOptions(), router);
    Schema mainSchema = parser.parse();
    mainSchema.validate("").setHandler(context.asyncAssertSuccess(o -> { // Trigger validation to start solve refs
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI), mainSchema.getScope(), "main");
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI).append("definitions").append("sub1"), mainSchema.getScope(), "sub1");
    }));
  }

  @Test
  public void relativeLocalRef(TestContext context) {
    URI sampleURI = URI.create("./sample.json");
    JsonObject mainSchemaUnparsed = new JsonObject().put("$ref", sampleURI.toString());
    parser = OpenAPI3SchemaParser.create(mainSchemaUnparsed, buildBasePath("test_2.json").toUri(), new SchemaParserOptions(), router);
    Schema mainSchema = parser.parse();
    mainSchema.validate("").setHandler(context.asyncAssertSuccess(o -> { // Trigger validation to start solve refs
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI), mainSchema.getScope(), "main");
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI).append("definitions").append("sub1"), mainSchema.getScope(), "sub1");
    }));
  }

  @Test
  public void relativeLocalRefFromResources(TestContext context) throws URISyntaxException {
    URI sampleURI = getClass().getResource("/ref_test/sample.json").toURI();
    JsonObject mainSchemaUnparsed = new JsonObject().put("$ref", sampleURI.toString());
    parser = OpenAPI3SchemaParser.create(mainSchemaUnparsed, sampleURI.resolve("test_1.json"), new SchemaParserOptions(), router);
    Schema mainSchema = parser.parse();
    mainSchema.validate("").setHandler(context.asyncAssertSuccess(o -> { // Trigger validation to start solve refs
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI), mainSchema.getScope(), "main");
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI).append("definitions").append("sub1"), mainSchema.getScope(), "sub1");
    }));
  }

  @Test
  public void jarURIRelativization(TestContext context) throws URISyntaxException {
    URI sampleURI = getClass().getClassLoader().getResource("sample_in_jar.json").toURI();
    URI replaced1 = URIUtils.resolvePath(sampleURI, "empty_in_jar.json");
    URI replaced2 = URIUtils.resolvePath(sampleURI, "./empty_in_jar.json");
    context.assertEquals(replaced1, getClass().getClassLoader().getResource("empty_in_jar.json").toURI());
    context.assertEquals(replaced2, getClass().getClassLoader().getResource("empty_in_jar.json").toURI());
  }

  @Test
  public void relativeLocalRefFromClassLoader(TestContext context) throws URISyntaxException {
    URI sampleURI = getClass().getClassLoader().getResource("sample_in_jar.json").toURI();
    JsonObject mainSchemaUnparsed = new JsonObject().put("$ref", sampleURI.toString());
    parser = OpenAPI3SchemaParser.create(mainSchemaUnparsed, URIUtils.resolvePath(sampleURI, "test_1.json"), new SchemaParserOptions(), router);
    Schema mainSchema = parser.parse();
    mainSchema.validate("").setHandler(context.asyncAssertSuccess(o -> { // Trigger validation to start solve refs
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI), mainSchema.getScope(), "main");
      assertThatSchemaContainsXid(router, JsonPointer.fromURI(sampleURI).append("definitions").append("sub1"), mainSchema.getScope(), "sub1");
    }));
  }

}
