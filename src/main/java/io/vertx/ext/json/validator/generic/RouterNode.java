package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class RouterNode {
  private Schema thisSchema;
  private List<RefSchema> referringSchemas;
  private final Map<String, RouterNode> childs;
  private RouterNode parent;

  public RouterNode() {
    this(null, null);
  }

  public RouterNode(RouterNode parent) { this(null, parent); }

  public RouterNode(Schema thisSchema, RouterNode parent) {
    this.thisSchema = thisSchema;
    this.referringSchemas = new ArrayList<>();
    this.childs = new HashMap<>();
    this.parent = parent;
  }

  public void setThisSchema(Schema thisSchema) {
    this.thisSchema = thisSchema;
  }

  public Schema getThisSchema() {
    return thisSchema;
  }

  public boolean hasSchema() {
    return thisSchema != null;
  }

  public Map<String, RouterNode> getChilds() {
    return childs;
  }

  public Stream<RouterNode> flattened() {
    return Stream.concat(
        (thisSchema == null) ? Stream.empty() : Stream.of(this),
        childs.values().stream().flatMap(RouterNode::flattened)
    );
  }

  public Stream<RouterNode> reverseFlattened() {
    return Stream.concat(
        childs.values().stream().flatMap(RouterNode::flattened),
        (thisSchema == null) ? Stream.empty() : Stream.of(this)
    );
  }

  public RouterNode getParent() {
    return parent;
  }

  public void setParent(RouterNode parent) {
    this.parent = parent;
  }

  public void addReferringSchema(RefSchema refSchema) {
    referringSchemas.add(refSchema);
  }

  public List<RefSchema> getReferringSchemas() {
    return referringSchemas;
  }
}
