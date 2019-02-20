package io.vertx.ext.json.validator.generic;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.json.pointer.JsonPointerIterator;
import io.vertx.ext.json.validator.Schema;

import java.util.function.Consumer;

public class RouterNodeJsonPointerIterator implements JsonPointerIterator {

  RouterNode actualNode;
  final Consumer<RouterNode> onNext;

  public RouterNodeJsonPointerIterator(RouterNode actualNode) {
    this(actualNode, null);
  }

  public RouterNodeJsonPointerIterator(RouterNode actualNode, Consumer<RouterNode> onNext) {
    this.actualNode = actualNode;
    this.onNext = onNext;
    invokeOnNext();
  }

  @Override
  public boolean isObject() {
    return !isNull();
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
    return (!isNull()) && actualNode.getChilds().containsKey(key);
  }

  @Override
  public boolean nextObjectParameter(String key, boolean createOnMissing) {
    if (isObject()) {
      if (!objectContainsKey(key)) {
        if (createOnMissing) {
          RouterNode node = new RouterNode();
          this.actualNode.getChilds().put(key, node);
        } else {
          return false;
        }
      }

      actualNode = actualNode.getChilds().get(key);
      invokeOnNext();
      return true;
    }
    return false;
  }

  @Override
  public boolean nextArrayElement(int i) {
    return false;
  }

  @Override
  public void empty() {
    actualNode = null;
  }

  @Override
  public Object getCurrentValue() {
    return actualNode;
  }

  @Override
  public void setCurrentValue(Object value) {
    if (value instanceof Schema) {
      this.actualNode.setThisSchema((Schema) value);
    } else if (value instanceof RouterNode) {
      this.actualNode = (RouterNode) value;
    }
  }

  @Override
  public boolean writeObjectParameter(String key, Object value) {
    if (value instanceof Schema) {
      this.nextObjectParameter(key, true);
      this.actualNode.setThisSchema((Schema) value);
      return true;
    } else if (value instanceof RouterNode) {
      this.actualNode.getChilds().put(key, (RouterNode) value);
      return true;
    }
    return false;
  }

  @Override
  public boolean writeArrayElement(int i, @Nullable Object value) {
    return false;
  }

  @Override
  public boolean appendArrayElement(Object value) {
    return false;
  }

  private void invokeOnNext() {
    if (onNext != null) onNext.accept(this.actualNode);
  }
}
