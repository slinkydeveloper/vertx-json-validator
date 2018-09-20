package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.Schema;

import java.util.HashMap;
import java.util.Map;

class RouterNode {
  private Schema thisSchema;
  private final Map<String, RouterNode> childs;
  private RouterNode parent;

  public RouterNode() {
    this(null, null);
  }

  public RouterNode(RouterNode parent) { this(null, parent); }

  public RouterNode(Schema thisSchema, RouterNode parent) {
    this.thisSchema = thisSchema;
    this.childs = new HashMap<>();
    this.parent = parent;
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

  public RouterNode getParent() {
    return parent;
  }

  public void setParent(RouterNode parent) {
    this.parent = parent;
  }
}
