package io.vertx.ext.json.validator;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.generic.SchemaRouterImpl;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
public abstract class BaseIntegrationTest {

  @Rule
  public final JUnitSoftAssertions t = new JUnitSoftAssertions();

  @Rule
  public final Timeout timeout = Timeout.seconds(60);

  @Rule
  public final RunTestOnContext classContext = new RunTestOnContext(() -> Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(10000)));

  public static final Logger log = LoggerFactory.getLogger(BaseIntegrationTest.class);

  public static final int SCHEMA_SERVER_PORT = 1234;

  public Vertx vertx;
  HttpServer schemaServer;
  String testName;
  public String testFileName;
  JsonObject test;

  public static Iterable<Object[]> buildParameters(List<String> tests, Path tckPath) {
    return tests.stream()
        .map(f -> new AbstractMap.SimpleImmutableEntry<>(f, tckPath.resolve(f + ".json")))
        .map(p -> {
          try {
            return new AbstractMap.SimpleImmutableEntry<>(p.getKey(), Files.readAllLines(p.getValue(), Charset.forName("UTF8")));
          } catch (IOException e) {
            e.printStackTrace();
            return null;
          }
        })
        .filter(Objects::nonNull)
        .map(strings -> new AbstractMap.SimpleImmutableEntry<>(strings.getKey(), String.join("", strings.getValue())))
        .map(string -> new AbstractMap.SimpleImmutableEntry<>(string.getKey(), new JsonArray(string.getValue())))
        .flatMap(t -> t.getValue()
            .stream()
            .map(JsonObject.class::cast)
            .map(o -> new Object[]{t.getKey() + ": " + o.getString("description"), t.getKey(), o})
        )
        .collect(Collectors.toList());
  }

  private void startSchemaServer(TestContext context) throws Exception {
    Router r = Router.router(vertx);
    r.route("/*")
      .produces("application/json")
      .handler(StaticHandler.create(getRemotesPath()).setCachingEnabled(true));
    schemaServer = vertx.createHttpServer(new HttpServerOptions().setPort(SCHEMA_SERVER_PORT))
      .requestHandler(r::accept)
      .listen(context.asyncAssertSuccess());
  }

  private void stopSchemaServer(TestContext context) throws Exception {
    if (schemaServer != null) {
      Async async = context.async();
      try {
        schemaServer.close((asyncResult) -> {
          async.complete();
        });
      } catch (IllegalStateException e) { // Server is already open
        async.complete();
      }
    }
  }

  public BaseIntegrationTest(Object testName, Object testFileName, Object testObject) {
    this.testName = (String) testName;
    this.testFileName = (String) testFileName;
    this.test = (JsonObject) testObject;
  }

  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = classContext.vertx();
    startSchemaServer(context);
  }

  @After
  public void tearDown(TestContext context) throws Exception {
    stopSchemaServer(context);
  }

  private Optional<Map.Entry<SchemaParser, Schema>> buildSchema(Object schema) {
    try {
      return Optional.of(buildSchemaFunction(schema));
    } catch (Exception e) {
      t.fail("Something went wrong during schema initialization for test \"" + testName + "\"", e);
      return Optional.empty();
    }
  }

  private void assertThrow(Runnable r, Class<? extends Throwable> throwable, String testName, String testCaseName) {
    try {
      r.run();
      t.fail("\"%s\" -> \"%s\" should be invalid", testName, testCaseName);
    } catch (Throwable throwed) {
      t.assertThat(throwed).as("\"%s\" -> \"%s\" should be of type ValidationException", testName, testCaseName).isInstanceOf(throwable);
    }
  }

  private void assertNotThrow(Runnable r, String testName, String testCaseName) {
    try {
      r.run();
    } catch (Throwable throwed) {
      t.fail(String.format("\"%s\" -> \"%s\" should be valid", testName, testCaseName), throwed);
    }
  }

  private void validateSuccess(Schema schema, SchemaParser parser, Object obj, String testCaseName, TestContext context) {
    Async async = context.async();
    schema.validateAsync(obj).setHandler(event -> {
      if (event.failed())
        t.fail(String.format("\"%s\" -> \"%s\" should be valid", testName, testCaseName), event.cause());
      ((SchemaRouterImpl)parser.getSchemaRouter()).solveAllSchemaReferences(schema).setHandler(ar -> {
        t.assertThat(ar.succeeded()).isTrue().withFailMessage("Failed schema refs resolving with cause {}", ar.cause());
        t.assertThat(schema.isSync()).isTrue();
        assertNotThrow(() -> schema.validateSync(obj), testName, testCaseName);
        async.complete();
      });
//      t.assertThat(schema.isSync()).isTrue();
//      assertNotThrow(() -> schema.validateSync(obj), testName, testCaseName);
//      async.complete();
    });
  }

  private void validateFailure(Schema schema, SchemaParser parser, Object obj, String testCaseName, TestContext context) {
    Async async = context.async();
    schema.validateAsync(obj).setHandler(event -> {
      if (event.succeeded())
        t.fail("\"%s\" -> \"%s\" should be invalid", testName, testCaseName);
      else
        log.debug(event.cause().toString());
      ((SchemaRouterImpl)parser.getSchemaRouter()).solveAllSchemaReferences(schema).setHandler(ar -> {
        t.assertThat(ar.succeeded()).isTrue().withFailMessage("Failed schema refs resolving with cause {}", ar.cause());
        t.assertThat(schema.isSync()).isTrue();
        assertThrow(() -> schema.validateSync(obj), ValidationException.class, testName, testCaseName);
        async.complete();
      });
//      t.assertThat(schema.isSync()).isTrue();
//      assertThrow(() -> schema.validateSync(obj), ValidationException.class, testName, testCaseName);
//      async.complete();
    });
  }


  @Test
  public void test(TestContext context) {
    buildSchema(test.getValue("schema"))
        .ifPresent(t -> {
          for (Object tc : test.getJsonArray("tests").stream().collect(Collectors.toList())) {
            JsonObject testCase = (JsonObject) tc;
            if (testCase.getBoolean("valid"))
              validateSuccess(t.getValue(), t.getKey(), testCase.getValue("data"), testCase.getString("description"), context);
            else
              validateFailure(t.getValue(), t.getKey(), testCase.getValue("data"), testCase.getString("description"), context);
          }
        });
  }

  public abstract Map.Entry<SchemaParser, Schema> buildSchemaFunction(Object schema) throws URISyntaxException;

  public abstract String getSchemasPath();

  public abstract String getRemotesPath();
}
