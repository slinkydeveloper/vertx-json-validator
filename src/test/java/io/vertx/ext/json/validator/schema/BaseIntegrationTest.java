package io.vertx.ext.json.validator.schema;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    public final RunTestOnContext classContext = new RunTestOnContext(Vertx::vertx);

    public static final int SCHEMA_SERVER_PORT = 1234;

    Vertx vertx;
    HttpServer schemaServer;
    String testName;
    JsonObject test;

    public static Iterable<Object[]> buildParameters(List<String> tests) {
        return tests.stream()
                .map(f -> new AbstractMap.SimpleImmutableEntry<>(f, Paths.get("src", "test", "resources", "openapi3", f + ".json")))
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
                        .map(o -> new Object[]{t.getKey() + ": " + o.getString("description"), o})
                )
                .collect(Collectors.toList());
    }

    private void startSchemaServer(TestContext context) throws Exception {
        Router r = Router.router(vertx);
        r.route().handler(StaticHandler.create(getSchemasPath() + "/remotes"));
        schemaServer = vertx.createHttpServer(new HttpServerOptions().setPort(SCHEMA_SERVER_PORT))
                .requestHandler(r::accept).listen(context.asyncAssertSuccess());
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
            async.await();
        }
    }

    public BaseIntegrationTest(Object testName, Object testObject) {
        this.testName = (String) testName;
        this.test = (JsonObject) testObject;
    }

    @Before
    public void setUp(TestContext context) throws Exception {
        vertx = classContext.vertx();
        //startSchemaServer(context);
    }

    @After
    public void tearDown(TestContext context) throws Exception {
        //stopSchemaServer(context);
    }

    private Optional<Schema> buildSchema(String testName, JsonObject schema) {
        try {
            return Optional.of(buildSchemaFunction(schema));
        } catch (Exception e) {
            t.fail("Something went wrong during schema initialization for test \"" + testName +"\"", e);
            return Optional.empty();
        }
    }

    private void validateSuccess(Schema schema, Object obj, String testCaseName, TestContext context) {
        Async async = context.async();
        schema.validate(obj).setHandler(new Handler<AsyncResult>() {
            @Override
            public void handle(AsyncResult event) {
                if (event.failed())
                    t.fail(String.format("\"%s\" -> \"%s\" should be valid", testName, testCaseName), event.cause());
                async.complete();
            }
        });
        async.await();
    }

    private void validateFailure(Schema schema, Object obj, String testCaseName, TestContext context) {
        Async async = context.async();
        schema.validate(obj).setHandler(new Handler<AsyncResult>() {
            @Override
            public void handle(AsyncResult event) {
                if (event.succeeded())
                    t.fail("\"%s\" -> \"%s\" should be invalid", testName, testCaseName);
                async.complete();
            }
        });
        async.await();
    }

    @Test
    public void test(TestContext context) {
        buildSchema(testName, test.getJsonObject("schema"))
                .ifPresent(schema -> {
                    for (Object tc : test.getJsonArray("tests").stream().collect(Collectors.toList())) {
                        JsonObject testCase = (JsonObject)tc;
                        if (testCase.getBoolean("valid"))
                            validateSuccess(schema, testCase.getValue("data"), testCase.getString("description"), context);
                        else
                            validateFailure(schema, testCase.getValue("data"), testCase.getString("description"), context);
                    }
                });
    }

    public abstract Schema buildSchemaFunction(JsonObject schema);

    public abstract String getSchemasPath();

}
