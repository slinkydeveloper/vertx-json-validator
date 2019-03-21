package io.vertx.ext.json.schema.draft7.dsl;

import io.vertx.ext.json.schema.generic.dsl.SchemaType;
import org.junit.jupiter.api.Test;

import static io.vertx.ext.json.schema.asserts.MyAssertions.assertThat;
import static io.vertx.ext.json.schema.draft7.dsl.Keywords.type;
import static io.vertx.ext.json.schema.draft7.dsl.Schemas.intSchema;
import static io.vertx.ext.json.schema.draft7.dsl.Schemas.schema;

public class SchemaBuilderTest {

  @Test
  public void testNullableSchema() {
    assertThat(
        intSchema().nullable().toJson()
    )
        .removingEntry("$id")
        .extractingKey("type")
        .containsAllAndOnlyItems("integer", "null");
  }

  @Test
  public void testMultipleTypes() {
    assertThat(
        schema()
          .with(
              type(SchemaType.INT, SchemaType.STRING)
          )
        .toJson()
    )
        .removingEntry("$id")
        .extractingKey("type")
        .containsAllAndOnlyItems("integer", "string");
  }

}
