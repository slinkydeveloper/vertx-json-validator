package io.vertx.ext.json.validator.openapi3;

import io.vertx.ext.json.validator.Validator;
import io.vertx.ext.json.validator.generic.BaseSchema;
import io.vertx.ext.json.pointer.impl.JsonPointerList;

import java.util.concurrent.ConcurrentSkipListSet;

public class OpenAPI3Schema extends BaseSchema {
  public OpenAPI3Schema(Object schema, JsonPointerList scope, ConcurrentSkipListSet<Validator> validators) {
    super(schema, scope, validators);
  }
}
