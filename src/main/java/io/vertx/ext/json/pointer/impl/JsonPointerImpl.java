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

import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.JsonPointerIterator;
import io.vertx.ext.json.validator.generic.URIUtils;

import java.net.URI;
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
      decodedTokens = new ArrayList<>(Arrays
          .stream(uri.getFragment().split("/", -1))
          .map(this::unescape)
          .collect(Collectors.toList())
      );
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
  public URI getURIWithoutFragment() {
    return startingUri;
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
  public Object query(JsonPointerIterator input) {
    // I should threat this as a special condition because the empty string can be a json obj key!
    if (isRootPointer())
      return input.getRawValue();
    else {
      walkTillLastElement(input, false);
      String lastKey = decodedTokens.get(decodedTokens.size() - 1);
      if (input.isObject()) {
        return (input.nextObjectParameter(lastKey)) ? input.getRawValue() : null;
      } else if (input.isArray() && !"-".equals(lastKey)) {
        try {
          return (input.nextArrayElement(Integer.parseInt(lastKey))) ? input.getRawValue() : null;
        } catch (NumberFormatException e) {
          return null;
        }
      } else
        return null;
    }
  }

  @Override
  public JsonPointer copy() {
    return new JsonPointerImpl(this.startingUri, this.decodedTokens);
  }

  @Override
  public boolean write(JsonPointerIterator input, Object value, boolean createOnMissing) {
    if (isRootPointer())
      throw new IllegalStateException("writeObject() doesn't support root pointers");
    else {
      walkTillLastElement(input, createOnMissing);
      return !input.isNull() && writeLastElement(input, value);
    }
  }

  private void walkTillLastElement(JsonPointerIterator iterator, boolean createOnMissing) {
    for (int i = 0; i < decodedTokens.size() - 1; i++) {
      String k = decodedTokens.get(i);
      if (i == 0 && "".equals(k)) {
        continue; // Avoid errors with root empty string
      } else if (iterator.isObject()) {
        if (!iterator.nextObjectParameter(k)) { // Return false if missing object
          if (createOnMissing)
            iterator.createNewContainerAndNext(k);
          else {
            iterator.setRawValue(null);
            return;
          }
        }
      } else if (iterator.isArray()) {
        if (k.equals("-")) {
          iterator.setRawValue(null);
          return; // - is useful only on write!
        }
        try {
          if (!iterator.nextArrayElement(Integer.parseInt(k))) { // Return false if missing array element
            if (createOnMissing)
              iterator.createNewContainerAndNext(k);
            else {
              iterator.setRawValue(null);
              return;
            }
          }
        } catch (NumberFormatException e) {
          iterator.setRawValue(null);
          return;
        }
      } else {
        iterator.setRawValue(null);
        return;
      }
    }
  }

  private boolean writeLastElement(JsonPointerIterator input, Object value) {
    String lastKey = decodedTokens.get(decodedTokens.size() - 1);
    if (input.isObject()) {
      return input.writeObjectParameter(lastKey, value);
    } else if (input.isArray()) {
      if ("-".equals(lastKey)) { // Append to end
        return input.appendArrayElement(value);
      } else { // We have a index
        try {
          return input.writeArrayElement(Integer.parseInt(lastKey), value);
        } catch (NumberFormatException e) {
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
