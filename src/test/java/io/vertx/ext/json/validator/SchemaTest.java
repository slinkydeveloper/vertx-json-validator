package io.vertx.ext.json.validator;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.draft7.Draft7SchemaParser;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;

import static io.vertx.ext.json.validator.TestUtils.buildBaseUri;
import static io.vertx.ext.json.validator.TestUtils.loadJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(VertxExtension.class)
public class SchemaTest {

  @Test
  public void testAsyncToSync(Vertx vertx, VertxTestContext testContext) throws Exception {
    URI u = buildBaseUri("asyncToSync.json");
    JsonObject obj = loadJson(u);
    Schema schema = Draft7SchemaParser.parse(vertx, obj, u);

    schema.validateAsync(new JsonObject()).setHandler(testContext.succeeding(r -> {
      testContext.verify(() -> {
        assertThat(schema.isSync()).isTrue();
        assertThatExceptionOfType(ValidationException.class)
            .isThrownBy(() -> schema.validateSync(new JsonObject().put("hello", 0)));
      });
      testContext.completeNow();
    }));
  }

}
