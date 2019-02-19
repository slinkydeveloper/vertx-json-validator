package io.vertx.ext.json.pointer;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.impl.JsonPointerIteratorImpl;

/**
 * The JsonPointerIterator is used by the read/write algorithms of the json pointer to read/write the querying data structure <br/>
 *
 * You can implement this interface to query the structure you want using json pointers
 *
 */
@VertxGen
public interface JsonPointerIterator {

  /**
   * @return true if the current value is a queryable object
   */
  boolean isObject();

  /**
   * @return true if the current value is a queryable array
   */
  boolean isArray();

  /**
   * @return true if the current value is null/empty
   */
  boolean isNull();

  /**
   * @param key object key
   * @return true if current value is a queryable object that contains the specified key
   */
  boolean objectContainsKey(String key);

  /**
   * Move the iterator to the object parameter with specified key.
   *
   * @param key object key
   * @param createOnMissing If the current value is an object that doesn't contain the key, put an empty object at provided key
   * @return true if the operation is successful
   */
  boolean nextObjectParameter(String key, boolean createOnMissing);

  /**
   * Move the iterator the the array element at specified index
   * @param i array index
   * @return true if the operation is successful
   */
  boolean nextArrayElement(int i);

  /**
   * Clean the current value of the iterator (put it as null)
   */
  void empty();

  /**
   * Get current iterator value
   *
   * @return
   */
  @Nullable Object getCurrentValue();

  /**
   * Write object parameter at specified key
   *
   * @param key
   * @param value
   * @return true if the operation is successful
   */
  boolean writeObjectParameter(String key, @Nullable Object value);

  /**
   * Write array element at specified index
   * @param i
   * @param value
   * @return true if the operation is successful
   */
  boolean writeArrayElement(int i, @Nullable Object value);

  /**
   * Append array element
   * @param value
   * @return true if the operation is successful
   */
  boolean appendArrayElement(@Nullable Object value);

  /**
   * Create a JsonPointerIterator for a JsonObject
   * @param object
   * @return
   */
  static JsonPointerIterator create(JsonObject object) {
    return new JsonPointerIteratorImpl(object);
  }

  /**
   * Create a JsonPointerIterator for a JsonArray
   * @param array
   * @return
   */
  static JsonPointerIterator create(JsonArray array) {
    return new JsonPointerIteratorImpl(array);
  }

}
