package io.vertx.ext.json.schema.generic.dsl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.SchemaURNId;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class SchemaBuilder<T extends SchemaBuilder<?, ?>, K extends Keyword> {

    protected SchemaType type;
    protected Map<String, Supplier<Object>> keywords;
    protected URI id;
    T self;

    @SuppressWarnings("unchecked")
    public SchemaBuilder(SchemaType type) {
        this.type = type;
        this.keywords = new HashMap<>();
        this.id = new SchemaURNId().toURI();
        this.self = (T)this;
        if (type != null)
            type(type);
    }

    @Fluent
    public T alias(String alias) {
        this.id = new SchemaURNId(alias).toURI();
        return self;
    }

    @Fluent
    public T id(URI id) {
        this.id = id;
        return self;
    }

    @Fluent
    public T with(K... keywords) { //TODO choose another cooler name
        for (Keyword k: keywords) {
            this.keywords.put(k.getKeyword(), k.getValueSupplier());
        }
        return self;
    }

    @Fluent
    public T defaultValue(Object defaultValue) {
        keywords.put("default", () -> defaultValue);
        return self;
    }

    @Fluent
    public T fromJson(JsonObject object) {
        object.forEach(e -> keywords.put(e.getKey(), e::getValue));
        return self;
    }

    @Fluent
    public T nullable() {
        keywords.put("type", () -> new JsonArray().add(type.getName()).add("null"));
        return self;
    }

    @Fluent
    public T type(SchemaType type) {
        this.type = type;
        keywords.put("type", type::getName);
        return self;
    }

    public JsonObject toJson() {
        JsonObject res = keywords
                .entrySet()
                .stream()
                .collect(JsonObject::new, (jo, e) -> jo.put(e.getKey(), e.getValue().get()), JsonObject::mergeIn);
        res.put("$id", id.toString());
        return res;
    }

    public final Schema build(SchemaParser parser) {
        return parser.parse(toJson(), id);
    }

}