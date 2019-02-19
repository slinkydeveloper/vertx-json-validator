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
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class JsonPointerTest {

  @Test
  public void testParsing() {
    JsonPointer pointer = JsonPointer.from("/hello/world");
    assertThat(pointer.build()).isEqualTo("/hello/world");
  }

  @Test
  public void testParsingErrorWrongFirstElement() {
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> JsonPointer.from("bla/hello/world"));
  }

  @Test
  public void testEncodingParsing() {
    List<String> keys = new ArrayList<>();
    keys.add("");
    keys.add("hell/o");
    keys.add("worl~d");
    JsonPointer pointer = new JsonPointerImpl(URI.create("#"), keys);
    assertThat(pointer.build()).isEqualTo("/hell~1o/worl~0d");
  }

  @Test
  public void testURIParsing() {
    JsonPointer pointer = JsonPointer.fromURI(URI.create("http://www.example.org#/hello/world"));
    assertThat(pointer.build()).isEqualTo("/hello/world");
    assertThat(pointer.buildURI()).isEqualTo(URI.create("http://www.example.org#/hello/world"));
  }

  @Test
  public void testURIEncodedParsing() {
    JsonPointer pointer = JsonPointer.fromURI(URI.create("http://www.example.org#/hello/world/%5Ea"));
    assertThat(pointer.build()).isEqualTo("/hello/world/^a");
    assertThat(pointer.buildURI()).isEqualTo(URI.create("http://www.example.org#/hello/world/%5Ea"));
  }

  @Test
  public void testBuilding() {
    List<String> keys = new ArrayList<>();
    keys.add("");
    keys.add("hello");
    keys.add("world");
    JsonPointer pointer = new JsonPointerImpl(URI.create("#"), keys);
    assertThat(pointer.build()).isEqualTo("/hello/world");
  }

  @Test
  public void testURIBuilding() {
    JsonPointer pointer = JsonPointer.create().append("hello").append("world");
    assertThat(pointer.buildURI()).isEqualTo(URI.create("#/hello/world"));
  }

  @Test
  public void testEmptyBuilding() {
    JsonPointer pointer = JsonPointer.create();
    assertThat(pointer.build()).isEqualTo("");
    assertThat(pointer.buildURI()).isEqualTo(URI.create("#"));
  }

  @Test
  public void testJsonObjectQuerying() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    JsonPointer pointer = JsonPointer.from("/hello/world");
    assertThat(pointer.queryJson(obj)).isEqualTo(1);
  }

  @Test
  public void testJsonArrayQuerying() {
    JsonArray array = new JsonArray();
    array.add(new JsonObject()
        .put("hello",
            new JsonObject().put("world", 2).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        ));
    array.add(new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        ));
    assertThat(JsonPointer.from("/1/hello/world").queryJson(array)).isEqualTo(1);
    assertThat(JsonPointer.fromURI(URI.create("#/1/hello/world")).queryJson(array)).isEqualTo(1);
  }

  @Test
  public void testRootPointer() {
    JsonPointer pointer = JsonPointer.create();
    JsonArray array = new JsonArray();
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 2).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    array.add(obj);
    array.add(new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        ));

    assertThat(pointer.queryJson(array)).isEqualTo(array);
    assertThat(pointer.queryJson(obj)).isEqualTo(obj);
    assertThat(pointer.queryJson("hello")).isEqualTo("hello");
  }

  @Test
  public void testWrongUsageOfDashForQuerying() {
    JsonArray array = new JsonArray();
    array.add(new JsonObject()
        .put("hello",
            new JsonObject().put("world", 2).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        ));
    array.add(new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        ));
    JsonPointer pointer = JsonPointer.from("/-/hello/world");
    assertThat(pointer.queryJson(array)).isNull();
  }

  /*
    The following JSON strings evaluate to the accompanying values:

    ""           // the whole document
    "/foo"       ["bar", "baz"]
    "/foo/0"     "bar"
    "/"          0
    "/a~1b"      1
    "/c%d"       2
    "/e^f"       3
    "/g|h"       4
    "/i\\j"      5
    "/k\"l"      6
    "/ "         7
    "/m~0n"      8

   */
  @Test
  public void testRFCExample() {
    JsonObject obj = new JsonObject("   {\n" +
        "      \"foo\": [\"bar\", \"baz\"],\n" +
        "      \"\": 0,\n" +
        "      \"a/b\": 1,\n" +
        "      \"c%d\": 2,\n" +
        "      \"e^f\": 3,\n" +
        "      \"g|h\": 4,\n" +
        "      \"i\\\\j\": 5,\n" +
        "      \"k\\\"l\": 6,\n" +
        "      \" \": 7,\n" +
        "      \"m~n\": 8\n" +
        "   }");

    assertThat(JsonPointer.from("").queryJson(obj)).isEqualTo(obj);
    assertThat(JsonPointer.from("/foo").queryJson(obj)).isEqualTo(obj.getJsonArray("foo"));
    assertThat(JsonPointer.from("/foo/0").queryJson(obj)).isEqualTo(obj.getJsonArray("foo").getString(0));
    assertThat(JsonPointer.from("/").queryJson(obj)).isEqualTo(obj.getInteger(""));
    assertThat(JsonPointer.from("/a~1b").queryJson(obj)).isEqualTo(obj.getInteger("a/b"));
    assertThat(JsonPointer.from("/c%d").queryJson(obj)).isEqualTo(obj.getInteger("c%d"));
    assertThat(JsonPointer.from("/e^f").queryJson(obj)).isEqualTo(obj.getInteger("e^f"));
    assertThat(JsonPointer.from("/g|h").queryJson(obj)).isEqualTo(obj.getInteger("g|h"));
    assertThat(JsonPointer.from("/i\\\\j").queryJson(obj)).isEqualTo(obj.getInteger("i\\\\j"));
    assertThat(JsonPointer.from("/k\\\"l").queryJson(obj)).isEqualTo(obj.getInteger("k\\\"l"));
    assertThat(JsonPointer.from("/ ").queryJson(obj)).isEqualTo(obj.getInteger(" "));
    assertThat(JsonPointer.from("/m~0n").queryJson(obj)).isEqualTo(obj.getInteger("m~n"));
  }

  @Test
  public void testWriteJsonObject() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    Object toInsert = new JsonObject().put("github", "slinkydeveloper");
    assertThat(JsonPointer.from("/hello/francesco").writeJson(obj, toInsert)).isTrue();
    assertThat(JsonPointer.from("/hello/francesco").queryJson(obj)).isEqualTo(toInsert);
  }

  @Test
  public void testWriteWithCreateOnMissingJsonObject() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    Object toInsert = new JsonObject().put("github", "slinkydeveloper");
    assertThat(JsonPointer.from("/hello/users/francesco").write(new JsonPointerIteratorImpl(obj), toInsert, true)).isTrue();
    assertThat(JsonPointer.from("/hello/users/francesco").queryJson(obj)).isEqualTo(toInsert);
  }

  @Test
  public void testWriteJsonObjectOverride() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    Object toInsert = new JsonObject().put("github", "slinkydeveloper");
    assertThat(JsonPointer.from("/hello/world").writeJson(obj, toInsert)).isTrue();
    assertThat(JsonPointer.from("/hello/world").queryJson(obj)).isEqualTo(toInsert);
  }

  @Test
  public void testWriteJsonArray() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", new JsonObject()).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    JsonArray array = new JsonArray();
    array.add(obj.copy());
    array.add(obj.copy());
    Object toInsert = new JsonObject().put("github", "slinkydeveloper");
    assertThat(JsonPointer.from("/0/hello/world/francesco").writeJson(array, toInsert)).isTrue();
    assertThat(JsonPointer.from("/0/hello/world/francesco").queryJson(array)).isEqualTo(toInsert);
    assertThat(array.getValue(1)).isNotEqualTo(array.getValue(0));
  }

  @Test
  public void testWriteJsonArrayAppend() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    JsonArray array = new JsonArray();
    array.add(obj.copy());
    array.add(obj.copy());
    Object toInsert = new JsonObject().put("github", "slinkydeveloper");
    assertThat(JsonPointer.from("/-").writeJson(array, toInsert)).isTrue();
    assertThat(JsonPointer.from("/2").queryJson(array)).isEqualTo(toInsert);
    assertThat(array.getValue(1)).isEqualTo(array.getValue(0));
  }

  @Test
  public void testWriteJsonArraySubstitute() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    JsonArray array = new JsonArray();
    array.add(obj.copy());
    array.add(obj.copy());
    Object toInsert = new JsonObject().put("github", "slinkydeveloper");
    assertThat(JsonPointer.from("/0").writeJson(array, toInsert)).isTrue();
    assertThat(JsonPointer.from("/0").queryJson(array)).isEqualTo(toInsert);
    assertThat(array.getValue(1)).isNotEqualTo(array.getValue(0));
  }

  @Test
  public void testNestedWriteJsonArraySubstitute() {
    JsonObject obj = new JsonObject()
        .put("hello",
            new JsonObject().put("world", 1).put("worl", "wrong")
        ).put("helo",
            new JsonObject().put("world", "wrong").put("worl", "wrong")
        );
    JsonArray array = new JsonArray();
    array.add(obj.copy());
    array.add(obj.copy());
    JsonObject root = new JsonObject().put("array", array);

    Object toInsert = new JsonObject().put("github", "slinkydeveloper");
    assertThat(JsonPointer.from("/array/0").writeJson(root, toInsert)).isTrue();
    assertThat(JsonPointer.from("/array/0").queryJson(root)).isEqualTo(toInsert);
  }

  @Test
  public void testIllegalUsageOfWriteJsonArray() {
    JsonArray array = new JsonArray();
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> JsonPointer.create().writeJson(array, new JsonObject()));
  }

  @Test
  public void testIllegalUsageOfWriteJsonObject() {
    JsonObject object = new JsonObject();
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> JsonPointer.create().writeJson(object, new JsonObject()));
  }

}
