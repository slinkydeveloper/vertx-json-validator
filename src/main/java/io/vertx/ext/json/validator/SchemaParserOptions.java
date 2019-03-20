package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class SchemaParserOptions {

  // TODO should i remove it?

  public SchemaParserOptions() { }

  public SchemaParserOptions(JsonObject json) {}

  public JsonObject toJson() { return new JsonObject(); }

}
