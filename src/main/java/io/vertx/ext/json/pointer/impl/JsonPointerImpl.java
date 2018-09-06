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

package io.vertx.ext.json.pointer.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.generic.URIUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class JsonPointerImpl implements JsonPointer {

  final public static Pattern VALID_POINTER_PATTERN = Pattern.compile("([0-9]*|-)(\\/([\\u0000-\\u002E]|[\\u0030-\\u007D]|[\\u007F-\\u10FFFF]|\\~0|\\~1)*)*");

  URI startingUri;

  // Empty means a pointer to root
  List<String> decodedTokens;

  private JsonPointerImpl(URI startingUri, List<String> decodedTokens) {
    this.startingUri = startingUri;
    this.decodedTokens = new ArrayList<>(decodedTokens);
  }

  public JsonPointerImpl(List<String> tokens) {
    this.startingUri = URI.create("#");
    if (tokens == null || tokens.size() == 0 || tokens.size() == 1 && "".equals(tokens.get(0))) {
      decodedTokens = new ArrayList<>();
      decodedTokens.add(""); // Root
    } else
      decodedTokens = new ArrayList<>(tokens);
  }

  public JsonPointerImpl(URI uri) {
    this.startingUri = URIUtils.replaceFragment(uri, null);
    if (uri.getFragment() != null && !uri.getFragment().isEmpty()) {
      decodedTokens = new ArrayList<String>(Arrays.asList(uri.getFragment().split("/", -1)));
      if (decodedTokens.size() == 0) decodedTokens.add("");
    } else {
      decodedTokens = new ArrayList<>();
      decodedTokens.add(""); // Root
    }
  }

  public JsonPointerImpl(String pointer) {
    this.startingUri = URI.create("#");
    decodedTokens = parse(pointer);
  }

  public JsonPointerImpl() {
    this.startingUri = URI.create("#");
    decodedTokens = new ArrayList<>();
    decodedTokens.add(""); // Root
  }

  private List<String> parse(String pointer) {
    if ("".equals(pointer)) {
      List<String> newList = new ArrayList<>();
      newList.add("");
      return newList;
    }
    if (VALID_POINTER_PATTERN.matcher(pointer).matches()) {
      return Arrays
          .stream(pointer.split("\\/", -1))
          .map(this::unescape)
          .collect(Collectors.toList());
    } else
      throw new IllegalArgumentException("The provided pointer is not a valid JSON Pointer");
  }

  private String escape(String path) {
    return path.replace("~", "~0")
        .replace("/", "~1");
  }

  private String unescape(String path) {
    return path.replace("~1", "/") // https://tools.ietf.org/html/rfc6901#section-4
        .replace("~0", "~");
  }

  @Override
  public boolean isRootPointer() {
    return decodedTokens == null || decodedTokens.size() == 0 || (decodedTokens.size() == 1 && "".equals(decodedTokens.get(0)));
  }

  @Override
  public String build() {
    if (isRootPointer())
      return "";
    else
      return String.join("/", decodedTokens.stream().map(this::escape).collect(Collectors.toList()));
  }

  @Override
  public URI buildURI() {
    if (isRootPointer()) {
      return URIUtils.replaceFragment(this.startingUri, "");
    } else
      return URIUtils.replaceFragment(
          this.startingUri,
          String.join("/", decodedTokens)
      );
  }

  @Override
  public JsonPointer append(String path) {
    decodedTokens.add(path);
    return this;
  }

  @Override
  public JsonPointer append(List<String> paths) {
    decodedTokens.addAll(paths);
    return this;
  }

  @Override
  public Object query(Object input) {
    // I should threat this as a special condition because the empty string can be a json obj key!
    if (isRootPointer())
      return input;
    else {
      Object v = walkTillLastElement(input);
      String lastKey = decodedTokens.get(decodedTokens.size() - 1);
      if (v instanceof JsonObject) {
        return ((JsonObject) v).getValue(lastKey);
      } else if (v instanceof JsonArray && !"-".equals(lastKey)) {
        try {
          return ((JsonArray) v).getValue(Integer.parseInt(lastKey));
        } catch (Exception e) {
          return null;
        }
      } else
        return null;
    }
  }

  @Override
  public boolean writeObject(JsonObject input, Object value) {
    return write(input, value);
  }

  @Override
  public boolean writeArray(JsonArray input, Object value) {
    return write(input, value);
  }

  @Override
  public JsonPointer copy() {
    return new JsonPointerImpl(this.startingUri, this.decodedTokens);
  }

  public URI getStartingUri() {
    return this.startingUri;
  }

  private boolean write(Object input, Object value) {
    if (isRootPointer())
      throw new IllegalStateException("writeObject() doesn't support root pointers");
    else {
      Object lastElem = walkTillLastElement(input);
      return lastElem != null && writeLastElement(lastElem, value);
    }
  }

  private Object walkTillLastElement(Object input) {
    for (int i = 0; i < decodedTokens.size() - 1; i++) {
      String k = decodedTokens.get(i);
      if (i == 0 && "".equals(k)) {
        continue; // Avoid errors with root empty string
      } else if (input instanceof JsonObject) {
        JsonObject obj = (JsonObject) input;
        if (obj.containsKey(k))
          input = obj.getValue(k);
        else
          return null; // Missing array element
      } else if (input instanceof JsonArray) {
        JsonArray arr = (JsonArray) input;
        if (k.equals("-"))
          return null; // - is useful only on write!
        else {
          try {
            input = arr.getValue(Integer.parseInt(k));
          } catch (Exception e) {
            return null;
          }
        }
      } else {
        return null;
      }
    }
    return input;
  }

  private boolean writeLastElement(Object input, Object value) {
    String lastKey = decodedTokens.get(decodedTokens.size() - 1);
    if (input instanceof JsonObject) {
      ((JsonObject) input).put(lastKey, value);
      return true;
    } else if (input instanceof JsonArray) {
      if ("-".equals(lastKey)) { // Append to end
        ((JsonArray) input).add(value);
        return true;
      } else { // We have a index
        try {
          ((JsonArray) input).getList().set(Integer.parseInt(lastKey), value);
          return true;
        } catch (Exception e) {
          return false;
        }
      }
    } else
      return false;
  }

  @Override
  public String toString() {
    return this.buildURI().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JsonPointerImpl that = (JsonPointerImpl) o;
    return Objects.equals(startingUri, that.startingUri) &&
        Objects.equals(decodedTokens, that.decodedTokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startingUri, decodedTokens);
  }
}
