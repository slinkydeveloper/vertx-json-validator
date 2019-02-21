package io.vertx.ext.json.validator.asserts;

import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaRouter;

public class MyAssertions {

  public static SchemaAssert assertThat(Schema actual) {
    return new SchemaAssert(actual);
  }

  public static SchemaRouterAssert assertThat(SchemaRouter actual) {
    return new SchemaRouterAssert(actual);
  }

}
