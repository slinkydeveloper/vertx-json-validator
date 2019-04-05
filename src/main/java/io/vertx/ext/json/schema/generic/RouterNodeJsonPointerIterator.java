package io.vertx.ext.json.schema.generic;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.pointer.JsonPointerIterator;
import io.vertx.ext.json.schema.Schema;

import java.util.function.Consumer;

// I know I'm violating the contract of the JsonPointerIterator and I'm keeping the internal state
class RouterNodeJsonPointerIterator implements JsonPointerIterator {

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
  public boolean isObject(Object value) {
    return !isNull(value);
  }

  @Override
  public boolean isArray(Object value) {
    return false;
  }

  @Override
  public boolean isNull(Object value) {
    return value == null;
  }

  @Override
  public boolean objectContainsKey(Object value, String key) {
    return !isNull(value) && ((RouterNode)value).getChilds().containsKey(key);
  }

  @Override
  public Object getObjectParameter(Object value, String key, boolean createOnMissing) {
    if (isObject(value)) {
      if (!objectContainsKey(value, key)) {
        if (createOnMissing) {
          RouterNode node = new RouterNode();
          this.actualNode.getChilds().put(key, node);
        } else {
          return null;
        }
      }

      actualNode = actualNode.getChilds().get(key);
      invokeOnNext();
      return actualNode;
    }
    return null;
  }

  @Override
  public Object getArrayElement(Object value, int i) {
    return null;
  }

  public RouterNode getCurrentValue() {
    return actualNode;
  }

  public void setCurrentValue(Object value) {
    if (value instanceof Schema) {
      this.actualNode.setSchema((Schema) value);
    } else if (value instanceof RouterNode) {
      this.actualNode = (RouterNode) value;
    }
  }

  @Override
  public boolean writeObjectParameter(Object value, String key, Object newElement) {
    if (newElement instanceof Schema) {
      this.getObjectParameter(value, key, true);
      this.actualNode.setSchema((Schema) newElement);
      return true;
    } else if (newElement instanceof RouterNode) {
      this.actualNode.getChilds().put(key, (RouterNode) newElement);
      return true;
    }
    return false;
  }

  @Override
  public boolean writeArrayElement(Object value, int i, @Nullable Object newElement) {
    return false;
  }

  @Override
  public boolean appendArrayElement(Object value, Object newElement) {
    return false;
  }

  private void invokeOnNext() {
    if (onNext != null) onNext.accept(this.actualNode);
  }
}
