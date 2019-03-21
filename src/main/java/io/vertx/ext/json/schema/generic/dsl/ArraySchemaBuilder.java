package io.vertx.ext.json.schema.generic.dsl;

import io.vertx.codegen.annotations.Fluent;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class ArraySchemaBuilder extends SchemaBuilder<ArraySchemaBuilder, ArrayKeyword> {

  // For items keyword as list of schemas
  private List<SchemaBuilder> itemList;

  ArraySchemaBuilder() {
    super(SchemaType.ARRAY);
    this.itemList = new LinkedList<>();
  }

  @Fluent
  public ArraySchemaBuilder items(SchemaBuilder schemaBuilder) {
    Objects.requireNonNull(schemaBuilder);
    this.keywords.put("items", schemaBuilder::toJson);
    return this;
  }

  @Fluent
  public ArraySchemaBuilder item(SchemaBuilder schemaBuilder) {
    Objects.requireNonNull(schemaBuilder);
    this.itemList.add(schemaBuilder);
    return this;
  }

  @Fluent
  public ArraySchemaBuilder additionalItems(SchemaBuilder schemaBuilder) {
    Objects.requireNonNull(schemaBuilder);
    this.keywords.put("additionalItems", schemaBuilder::toJson);
    return this;
  }

}
