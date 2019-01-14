package io.vertx.ext.json.validator;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.draft7.Draft7SchemaParser;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(VertxUnitRunner.class)
public class SchemaTest {

  @Rule
  public final RunTestOnContext classContext = new RunTestOnContext(Vertx::vertx);

  private JsonObject loadJson(URI uri) throws IOException {
    return new JsonObject(String.join("", Files.readAllLines(Paths.get(uri))));
  }

    private URI buildBaseUri(String filename) {
      return Paths.get("src", "test", "resources", filename).toAbsolutePath().toUri();
    }

  private void assertThrow(Runnable r, Class<? extends Throwable> t, TestContext context) {
    try {
      r.run();
      context.fail("No exception thrown");
    } catch (Throwable throwed) {
      context.assertEquals(t, throwed.getClass());
    }
  }

  @Test
  public void testAsyncToSync(TestContext context) throws Exception {
    URI u = buildBaseUri("asyncToSync.json");
    JsonObject obj = loadJson(u);
    Schema schema = Draft7SchemaParser.parse(classContext.vertx(), obj, u);

    schema.validateAsync(new JsonObject()).setHandler(context.asyncAssertSuccess(r -> {
      context.assertTrue(schema.isSync());
      assertThrow(() -> schema.validateSync(new JsonObject().put("hello", 0)), ValidationException.class, context);
    }));
  }

}
