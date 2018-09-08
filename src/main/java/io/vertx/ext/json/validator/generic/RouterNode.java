package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaRouter;

import java.util.HashMap;
import java.util.Map;

class RouterNode {
  private Schema thisSchema;
  private final Map<String, RouterNode> childs;

  public RouterNode() {
    this(null);
  }

  public RouterNode(Schema thisSchema) {
    this.thisSchema = thisSchema;
    this.childs = new HashMap<>();
  }

  public void setThisSchema(Schema thisSchema) {
    this.thisSchema = thisSchema;
  }

  public Schema getThisSchema() {
    return thisSchema;
  }

  public Map<String, RouterNode> getChilds() {
    return childs;
  }
}
