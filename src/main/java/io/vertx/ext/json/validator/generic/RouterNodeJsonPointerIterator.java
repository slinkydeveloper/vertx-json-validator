package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.pointer.JsonPointerIterator;
import io.vertx.ext.json.validator.Schema;

public class RouterNodeJsonPointerIterator implements JsonPointerIterator {

  RouterNode actualNode;

  public RouterNodeJsonPointerIterator(RouterNode actualNode) {
    this.actualNode = actualNode;
  }

  @Override
  public boolean isObject() {
    return true;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isNull() {
    return actualNode == null;
  }

  @Override
  public boolean objectContainsKey(String key) {
    return actualNode.getChilds().containsKey(key);
  }

  @Override
  public boolean nextObjectParameter(String parameterName) {
    if (objectContainsKey(parameterName)) {
      actualNode = actualNode.getChilds().get(parameterName);
      return true;
    }
    return false;
  }

  @Override
  public boolean nextArrayElement(Integer i) {
    return false;
  }

  @Override
  public void setRawValue(Object value) {
    actualNode = (RouterNode) value;
  }

  @Override
  public Object getRawValue() {
    return actualNode;
  }

  @Override
  public boolean writeObjectParameter(String key, Object value) {
    if (value instanceof Schema) {
      this.createNewContainerAndNext(key);
      this.actualNode.setThisSchema((Schema) value);
      return true;
    } else if (value instanceof RouterNode) {
      this.actualNode.getChilds().put(key, (RouterNode) value);
      return true;
    }
    return false;
  }

  @Override
  public boolean writeArrayElement(Integer i, Object value) {
    return false;
  }

  @Override
  public boolean appendArrayElement(Object value) {
    return false;
  }

  @Override
  public boolean createNewContainerAndNext(String k) {
    RouterNode node = new RouterNode();
    this.actualNode.getChilds().put(k, node);
    this.actualNode = node;
    return true;
  }
}
