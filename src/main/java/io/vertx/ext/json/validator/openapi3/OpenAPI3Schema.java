package io.vertx.ext.json.validator.openapi3;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.Validator;
import io.vertx.ext.json.validator.generic.BaseSchema;

import java.util.concurrent.ConcurrentSkipListSet;

public class OpenAPI3Schema extends BaseSchema {
    public OpenAPI3Schema(Object schema, ConcurrentSkipListSet<Validator> validators) {
        super(schema, validators);
    }
}
