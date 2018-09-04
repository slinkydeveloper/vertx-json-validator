package io.vertx.ext.json.pointer.impl;

import io.vertx.ext.json.pointer.JsonPointer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class JsonPointerList extends ArrayList<JsonPointer> {

    public JsonPointerList() {
        super();
    }

    public JsonPointerList(Collection<? extends JsonPointer> c) {
        super(c);
    }

    public JsonPointerList copyList() {
        return new JsonPointerList(
                this.stream().filter(jp -> ((JsonPointerImpl)jp).undecodedTokens.get(0).isEmpty()).map(JsonPointer::copy).collect(Collectors.toList())
        );
    }

    public JsonPointerList appendToAllPointers(final String path) {
        this.forEach(j -> j.append(path));
        return this;
    }
}
