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
import java.util.stream.IntStream;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class JsonPointerImpl implements JsonPointer {

  final public static Pattern VALID_POINTER_PATTERN = Pattern.compile("^(/(([^/~])|(~[01]))*)*$");

  URI startingUri;

  // Empty means a pointer to root
  List<String> decodedTokens;

  public JsonPointerImpl(URI uri) {
    this.startingUri = URIUtils.removeFragment(uri);
    this.decodedTokens = parse(uri.getFragment());
  }

  public JsonPointerImpl(String pointer) {
    this.startingUri = URI.create("#");
    this.decodedTokens = parse(pointer);
  }

  public JsonPointerImpl() {
    this.startingUri = URI.create("#");
    this.decodedTokens = parse(null);
  }

  protected JsonPointerImpl(URI startingUri, List<String> decodedTokens) {
    this.startingUri = startingUri;
    this.decodedTokens = new ArrayList<>(decodedTokens);
  }

  private ArrayList<String> parse(String pointer) {
    if (pointer == null || "".equals(pointer)) {
      return new ArrayList<>();
    }
    if (VALID_POINTER_PATTERN.matcher(pointer).matches()) {
      return Arrays
          .stream(pointer.split("\\/", -1))
          .skip(1) //Ignore first element
          .map(this::unescape)
          .collect(Collectors.toCollection(ArrayList::new));
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
    return decodedTokens.size() == 0;
  }

  @Override
  public boolean isLocalPointer() {
    return startingUri == null || startingUri.getSchemeSpecificPart() == null || startingUri.getSchemeSpecificPart().isEmpty();
  }

  @Override
  public boolean isParent(JsonPointer child) {
    return child != null &&
        (child.getURIWithoutFragment() == null && this.getURIWithoutFragment() == null || child.getURIWithoutFragment().equals(this.getURIWithoutFragment())) &&
        IntStream.range(0, decodedTokens.size() - 1)
            .mapToObj(i -> this.decodedTokens.get(i).equals(((JsonPointerImpl) child).decodedTokens.get(i)))
            .reduce(Boolean::logicalAnd).orElse(false);
  }

  @Override
  public String build() {
    if (isRootPointer())
      return "";
    else
      return "/" + String.join("/", decodedTokens.stream().map(this::escape).collect(Collectors.toList()));
  }

  @Override
  public URI buildURI() {
    if (isRootPointer()) {
      return URIUtils.replaceFragment(this.startingUri, "");
    } else
      return URIUtils.replaceFragment(
          this.startingUri,
          "/" + String.join("/", decodedTokens)
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
  public JsonPointer parent() {
    if (!this.isRootPointer()) decodedTokens.remove(decodedTokens.size() - 1);
    return this;
  }

  @Override
  public JsonPointer copy() {
    return new JsonPointerImpl(this.startingUri, this.decodedTokens);
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

  @Override
  public Object query(JsonPointerIterator input) {
    // I should threat this as a special condition because the empty string can be a json obj key!
    if (isRootPointer())
      return input.getCurrentValue();
    else {
      walkTillLastElement(input, false);
      String lastKey = decodedTokens.get(decodedTokens.size() - 1);
      if (input.isObject()) {
        return (input.nextObjectParameter(lastKey, false)) ? input.getCurrentValue() : null;
      } else if (input.isArray() && !"-".equals(lastKey)) {
        try {
          return (input.nextArrayElement(Integer.parseInt(lastKey))) ? input.getCurrentValue() : null;
        } catch (NumberFormatException e) {
          return null;
        }
      } else
        return null;
    }
  }

  @Override
  public boolean write(JsonPointerIterator input, Object value, boolean createOnMissing) {
    if (isRootPointer()) {
      input.setCurrentValue(value);
      return true;
    } else {
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
        if (!iterator.nextObjectParameter(k, createOnMissing)) { // Return false if missing object
          iterator.empty();
          return;
        }
      } else if (iterator.isArray()) {
        try {
          if (!iterator.nextArrayElement(Integer.parseInt(k))) { // Return false if missing array element
            if (createOnMissing)
              iterator.nextObjectParameter(k, createOnMissing);
            else {
              iterator.empty();
              return;
            }
          }
        } catch (NumberFormatException e) {
          iterator.empty();
          return;
        }
      } else {
        iterator.empty();
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
}
