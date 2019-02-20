/*
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.ext.json.pointer;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.json.pointer.impl.JsonPointerImpl;
import io.vertx.ext.json.pointer.impl.JsonPointerIteratorImpl;

import java.net.URI;
import java.util.List;

/**
 * Implementation of <a href="https://tools.ietf.org/html/rfc6901">RFC6901 Json Pointers</a>.
 *
 * @author Francesco Guardiani <a href="https://slinkydeveloper.github.io/">@slinkydeveloper</a>
 */
@VertxGen
public interface JsonPointer {

  /**
   * Return true if the pointer is a root pointer
   *
   * @return
   */
  boolean isRootPointer();

  /**
   * Return true if the pointer is local (URI with only fragment)
   *
   * @return
   */
  boolean isLocalPointer();

  boolean isParent(JsonPointer child);

  /**
   * Alias for toString()
   *
   * @return
   */
  String build();

  /**
   * Build a URI representation of the JSON Pointer
   *
   * @return
   */
  URI buildURI();

  /**
   * Returns the underlying URI without the fragment
   *
   * @return
   */
  URI getURIWithoutFragment();

  /**
   * Append unescaped path to JsonPointer. <br/>
   * Note: If you provide escaped path the behaviour is undefined
   *
   * @param path unescaped path
   * @return
   */
  @Fluent
  JsonPointer append(String path);

  /**
   * Append unescaped list of paths to JsonPointer <br/>
   * Note: If you provide escaped paths the behaviour is undefined
   *
   * @param paths unescaped paths
   * @return
   */
  @Fluent
  JsonPointer append(List<String> paths);

  /**
   * Remove last append
   *
   * @return
   */
  @Fluent
  JsonPointer parent();

  /**
   * Query the provided readable json pointer iterator. <br/>
   * Note: if this pointer is a root pointer, this function returns the provided object
   *
   * @param object the readable json pointer iterator to query. This object will mutate internally
   * @return null if pointer points to not existing value, otherwise the requested value
   */
  @Nullable Object query(JsonPointerIterator object);

  /**
   * Query the provided object. <br/>
   * Note: if this pointer is a root pointer, this function returns the provided object
   *
   * @param object the object to queryJson
   * @return null if pointer points to not existing value, otherwise the requested value
   */
  default @Nullable Object queryJson(Object object) {return query(new JsonPointerIteratorImpl(object)); }

  /**
   * Write a value with the selected pointer. The path token "-" is handled as append to end of array
   *
   * @param iterator iterator to query and write
   * @param value  object to insert
   * @param createOnMissing create objects when missing a object key or an array index
   * @return true if the write is completed, false otherwise
   * @throws IllegalStateException if the pointer is a root pointer
   */
  boolean write(JsonPointerIterator iterator, Object value, boolean createOnMissing);

  /**
   * Write a value in the selected pointer. The path token "-" is handled as append to end of array. <br/>
   * This function does not support root pointers.
   *
   * @param json json to query and write
   * @param valueToInsert json to insert
   * @return true if the write is completed, false otherwise
   * @throws IllegalStateException if the pointer is a root pointer
   */
  default boolean writeJson(Object json, Object valueToInsert) { return writeJson(json, valueToInsert, false); }

  /**
   * Write a value in the selected pointer. The path token "-" is handled as append to end of array. <br/>
   * This function does not support root pointers.
   *
   * @param json json to query and write
   * @param valueToInsert json to insert
   * @param createOnMissing create JsonObject when missing a object key or an array index
   * @return true if the write is completed, false otherwise
   * @throws IllegalStateException if the pointer is a root pointer
   */
  default boolean writeJson(Object json, Object valueToInsert, boolean createOnMissing) {
    if (isRootPointer())
      throw new IllegalStateException("writeJson() doesn't support root pointers. You must use write() and manually create the iterator");
    return write(new JsonPointerIteratorImpl(json), valueToInsert, createOnMissing);
  }

  /**
   * Copy a JsonPointer
   *
   * @return
   */
  JsonPointer copy();

  /**
   * Build an empty JsonPointer
   *
   * @return a new empty JsonPointer
   */
  static JsonPointer create() {
    return new JsonPointerImpl();
  }

  /**
   * Build a JsonPointer from a json pointer string
   *
   * @param pointer the string representing a pointer
   * @return new instance of JsonPointer
   * @throws IllegalArgumentException if the pointer provided is not valid
   */
  static JsonPointer from(String pointer) {
    return new JsonPointerImpl(pointer);
  }

  /**
   * Build a JsonPointer from a URI.
   *
   * @param uri uri representing a json pointer
   * @return new instance of JsonPointer
   * @throws IllegalArgumentException if the pointer provided is not valid
   */
  static JsonPointer fromURI(URI uri) {
    return new JsonPointerImpl(uri);
  }

}
