package io.vertx.ext.json.pointer;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.impl.JsonPointerIteratorImpl;

public interface JsonPointerIterator {

  boolean isObject();

  boolean isArray();

  boolean isNull();

  boolean objectContainsKey(String key);

  boolean nextObjectParameter(String parameterName);

  boolean nextArrayElement(Integer i);

  void setRawValue(Object value);

  Object getRawValue();

  boolean writeObjectParameter(String key, Object value);

  boolean writeArrayElement(Integer i, Object value);

  boolean appendArrayElement(Object value);

  boolean createNewContainerAndNext(String k);

  static JsonPointerIterator create(JsonObject object) {
    return new JsonPointerIteratorImpl(object);
  }

  static JsonPointerIterator create(JsonArray array) {
    return new JsonPointerIteratorImpl(array);
  }

}
