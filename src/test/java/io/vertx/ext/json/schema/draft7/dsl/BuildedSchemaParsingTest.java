package io.vertx.ext.json.schema.draft7.dsl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.*;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.ext.json.schema.draft7.dsl.Keywords.exclusiveMaximum;
import static io.vertx.ext.json.schema.draft7.dsl.Keywords.multipleOf;
import static io.vertx.ext.json.schema.draft7.dsl.Schemas.*;

@ExtendWith(VertxExtension.class)
public class BuildedSchemaParsingTest {

  SchemaRouter router;
  SchemaParser parser;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(new SchemaParserOptions(), router);
  }

  @Test
  public void testCircularTreeDeclaration(VertxTestContext testContext) {
    Schema schema =
        objectSchema()
            .alias("root_object")
            .requiredProperty("value",
                intSchema()
                    .with(exclusiveMaximum(20d), multipleOf(2d))
            )
            .property("leftChild", refToAlias("root_object"))
            .property("rightChild", refToAlias("root_object"))
            .build(parser);
    testContext.assertComplete(schema.validateAsync(
        new JsonObject().put("value", 6).put("leftChild", new JsonObject().put("value", 2))
    )).setHandler(v -> testContext.completeNow());
  }

}
