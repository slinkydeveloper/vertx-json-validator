package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.json.pointer.impl.JsonPointerList;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class BaseSchema implements Schema {

    private static final Logger log = LoggerFactory.getLogger(BaseSchema.class);

    private final JsonObject schema;
    private final JsonPointerList scope;
    private final ConcurrentSkipListSet<Validator> validators;

    public BaseSchema(Object schema, JsonPointerList scope, ConcurrentSkipListSet<Validator> validators) {
        this.schema = (JsonObject) schema;
        this.scope = scope;
        this.validators = validators;
    }

    @Override
    public JsonPointerList getIds() {
        return scope;
    }

    public JsonObject getSchema() {
        return schema;
    }

    @Override
    public Future validate(Object in) {
        List<Future> futures = new ArrayList<>();
        for (Validator validator : validators) {
            if (validator.isAsync()) futures.add(((AsyncValidator)validator).validate(in));
            else try {
                ((SyncValidator)validator).validate(in);
            } catch (ValidationException e) {
                return Future.failedFuture(e);
            }
        }
        return CompositeFuture.all(futures).compose(cf -> Future.succeededFuture());
    }

}
