package io.vertx.ext.json.pointer.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointerIterator;

import java.util.List;
import java.util.Map;

public class JsonPointerIteratorImpl implements JsonPointerIterator {

  Object value;

  public JsonPointerIteratorImpl(Object value) {
    this.value = value;
  }

  @Override
  public boolean isObject() {
    return value instanceof JsonObject;
  }

  @Override
  public boolean isArray() {
    return value instanceof JsonArray;
  }

  @Override
  public boolean isNull() {
    return value == null;
  }

  @Override
  public boolean objectContainsKey(String key) {
    return isObject() && ((JsonObject)value).containsKey(key);
  }

  @Override
  public boolean nextObjectParameter(String key, boolean createOnMissing) {
    if (isObject()) {
      if (!objectContainsKey(key)) {
        if (createOnMissing) {
          writeObjectParameter(key, new JsonObject());
        } else {
          return false;
        }
      }
      value = jsonifyValue(((JsonObject) value).getValue(key));
      return true;
    }
    return false;
  }

  @Override
  public boolean nextArrayElement(int i) {
    if (isArray()) {
      try {
        value = jsonifyValue(((JsonArray)value).getValue(i));
        return true;
      } catch (IndexOutOfBoundsException e) {
        return false;
      }
    }
    return false;
  }

  @Override
  public void empty() {
    this.value = null;
  }

  @Override
  public Object getCurrentValue() {
    return value;
  }

  @Override
  public void setCurrentValue(Object obj) {
    this.value = obj;
  }

  @Override
  public boolean writeObjectParameter(String key, Object el) {
    if (isObject()) {
      ((JsonObject)value).put(key, el);
      return true;
    } else return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean writeArrayElement(int i, Object el) {
    if (isArray()) {
      try {
      ((JsonArray)value).getList().add(i, el);
      return true;
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
    } else return false;
  }

  @Override
  public boolean appendArrayElement(Object el) {
    if (isArray()) {
      ((JsonArray)value).add(el);
      return true;
    } else return false;
  }

  @SuppressWarnings("unchecked")
  private Object jsonifyValue(Object v) {
    if (v instanceof Map) return new JsonObject((Map<String, Object>)v);
    else if (v instanceof List) return new JsonArray((List)v);
    else return v;
  }
}
