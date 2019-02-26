package io.vertx.ext.json.validator.asserts;

import io.vertx.ext.json.pointer.JsonPointer;
import org.assertj.core.api.AbstractAssert;

public class JsonAssert extends AbstractAssert<JsonAssert, Object> {
  public JsonAssert(Object actual) {
    super(actual, JsonAssert.class);
  }

  public JsonAssert extracting(JsonPointer pointer) {
    return new JsonAssert(pointer.queryJson(actual));
  }

}
