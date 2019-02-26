package io.vertx.ext.json.validator.asserts;

import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.SchemaRouter;
import io.vertx.ext.json.validator.generic.SchemaImpl;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SchemaRouterAssert extends AbstractAssert<SchemaRouterAssert, SchemaRouter> {

  public SchemaRouterAssert(SchemaRouter actual) {
    super(actual, SchemaRouterAssert.class);
  }

  public SchemaAssert canResolveSchema(JsonPointer jp, JsonPointer scope, SchemaParser parser) {
    isNotNull();

    try {
      return new SchemaAssert(actual.resolveCachedSchema(jp, scope, parser));
    } catch (Exception e) {
      fail("Cannot resolve schema", e);
      return new SchemaAssert(null);
    }
  }

  public SchemaRouterAssert containsOnlyOneCachedSchemaWithXId(String expectedXId) {
    isNotNull();

    assertThat(actual.registeredSchemas())
        .isNotNull();

    assertThat(actual.registeredSchemas())
        .filteredOn(s -> s instanceof SchemaImpl)
        .extracting(s -> ((SchemaImpl)s).getJson())
        .extracting(j -> j.getString("x-id"))
        .areExactly(1, new Condition<>(expectedXId::equals, "Expected id {}", expectedXId));


    return this;
  }

  public SchemaRouterAssert containsCachedSchemasWithXIds(String... expectedXIds) {
    for (String id : expectedXIds) {
      containsOnlyOneCachedSchemaWithXId(id);
    }

    return this;
  }

}