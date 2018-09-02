package io.vertx.ext.json.validator;

import io.vertx.core.Future;

@FunctionalInterface
public interface Schema {
    Future validate(Object in);
}
