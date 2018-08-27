package io.vertx.ext.json.validator;

import io.vertx.core.Future;

public interface Schema {
    Future validate(Object in);
}
