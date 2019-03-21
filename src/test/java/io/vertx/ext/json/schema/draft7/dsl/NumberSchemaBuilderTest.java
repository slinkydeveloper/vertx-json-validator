package io.vertx.ext.json.schema.draft7.dsl;

import org.junit.jupiter.api.Test;

import static io.vertx.ext.json.schema.TestUtils.entry;
import static io.vertx.ext.json.schema.asserts.MyAssertions.assertThat;
import static io.vertx.ext.json.schema.draft7.dsl.Schemas.numberSchema;

public class NumberSchemaBuilderTest {

  @Test
  public void testNumberSchema(){
    assertThat(
        numberSchema().toJson()
    )   .removingEntry("$id")
        .containsAllAndOnlyEntries(entry("type", "number"));
  }

  @Test
  public void testIntegerSchema(){
    assertThat(
        numberSchema().asInteger().toJson()
    )   .removingEntry("$id")
        .containsAllAndOnlyEntries(entry("type", "integer"));
  }

}
