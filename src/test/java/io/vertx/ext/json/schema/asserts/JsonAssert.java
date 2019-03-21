package io.vertx.ext.json.schema.asserts;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import org.assertj.core.api.AbstractAssert;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonAssert extends AbstractAssert<JsonAssert, Object> {
  public JsonAssert(Object actual) {
    super(actual, JsonAssert.class);
  }

  public JsonAssert extracting(JsonPointer pointer) {
    return new JsonAssert(pointer.queryJson(actual));
  }

  public JsonAssert extractingKey(String keyName) {
    isJsonObject();
    return new JsonAssert(((JsonObject)actual).getValue(keyName));
  }

  public JsonAssert removingEntry(String keyName) {
    containsKey(keyName);

    JsonObject jo = ((JsonObject)actual).copy();
    jo.remove(keyName);

    return new JsonAssert(jo);
  }

  public JsonAssert isJsonObject() {
    assertThat(actual).isInstanceOf(JsonObject.class);
    return this;
  }

  public JsonAssert isJsonArray() {
    assertThat(actual).isInstanceOf(JsonArray.class);
    return this;
  }

  public JsonAssert containsEntry(String keyword, Object value) {
    isJsonObject();

    JsonObject jo = (JsonObject) actual;
    assertThat(jo.getValue(keyword)).isEqualTo(value);

    return this;
  }

  public JsonAssert containsKey(String keyword) {
    isJsonObject();

    JsonObject jo = (JsonObject) actual;
    assertThat(jo.containsKey(keyword)).isTrue();

    return this;
  }

  public JsonAssert containsItem(Object value) {
    isJsonArray();

    JsonArray ja = (JsonArray) actual;

    assertThat(ja.contains(value)).isTrue();

    return this;

  }

  public JsonAssert containsAllAndOnlyEntries(Map.Entry... entries) {
    isJsonObject();

    JsonObject jo = (JsonObject) actual;
    assertThat(jo.size()).isEqualTo(entries.length);
    for (Map.Entry<String, Object> e : entries) {
      assertThat(jo.getValue(e.getKey())).isEqualTo(e.getValue());
    }

    return this;
  }

  public JsonAssert containsAllAndOnlyItems(Object... items) {
    isJsonArray();

    JsonArray ja = (JsonArray) actual;
    assertThat(ja.size()).isEqualTo(items.length);
    for (Object i : items) {
      assertThat(ja.contains(i)).isTrue();
    }

    return this;
  }

}
