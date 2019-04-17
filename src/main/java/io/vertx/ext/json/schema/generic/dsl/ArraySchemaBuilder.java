package io.vertx.ext.json.schema.generic.dsl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class ArraySchemaBuilder extends SchemaBuilder<ArraySchemaBuilder, ArrayKeyword> {

  // For items keyword as list of schemas
  private List<SchemaBuilder> itemList;
  private SchemaBuilder additionalItems;

  ArraySchemaBuilder() {
    super(SchemaType.ARRAY);
    this.itemList = new LinkedList<>();
  }

  @Fluent
  public ArraySchemaBuilder items(SchemaBuilder schemaBuilder) {
    Objects.requireNonNull(schemaBuilder);
    if (itemList.size() > 0) throw new IllegalStateException("You can build or an item by item array schema or a same type items array schema");
    this.keywords.put("items", schemaBuilder::toJson);
    return this;
  }

  @Fluent
  public ArraySchemaBuilder item(SchemaBuilder schemaBuilder) {
    Objects.requireNonNull(schemaBuilder);
    if (this.keywords.containsKey("items")) throw new IllegalStateException("You can build or an item by item array schema or a same type items array schema");
    this.itemList.add(schemaBuilder);
    return this;
  }

  @Fluent
  public ArraySchemaBuilder additionalItems(SchemaBuilder schemaBuilder) {
    Objects.requireNonNull(schemaBuilder);
    if (this.keywords.containsKey("items")) throw new IllegalStateException("You can build or an item by item array schema or a same type items array schema");
    this.additionalItems = schemaBuilder;
    return this;
  }

  public List<SchemaBuilder> getItemList() {
    return itemList;
  }

  public SchemaBuilder getAdditionalItems() {
    return additionalItems;
  }

  public boolean isItemByItemArraySchema() {
    return additionalItems != null || itemList.size() > 0;
  }

  @Override
  public JsonObject toJson() {
    if (!itemList.isEmpty())
      this.keywords.put("items", () -> itemList.stream().collect(JsonArray::new, (ja, s) -> ja.add(s.toJson()), JsonArray::addAll));
    if (additionalItems != null)
      this.keywords.put("additionalItems", additionalItems::toJson);
    return super.toJson();
  }
}
