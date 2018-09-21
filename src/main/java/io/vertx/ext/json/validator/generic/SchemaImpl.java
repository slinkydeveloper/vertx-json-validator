package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class SchemaImpl implements Schema {

  private static final Logger log = LoggerFactory.getLogger(SchemaImpl.class);

  private final JsonObject schema;
  private final JsonPointer scope;
  private final ConcurrentSkipListSet<Validator> validators;

  public SchemaImpl(JsonObject schema, JsonPointer scope, ConcurrentSkipListSet<Validator> validators) {
    this.schema = schema;
    this.scope = scope;
    this.validators = validators;
  }

  @Override
  public JsonPointer getScope() {
    return scope;
  }

  public JsonObject getSchema() {
    return schema;
  }

  @Override
  public Future validate(Object in) {
    if (log.isDebugEnabled()) log.debug("Starting validation for schema {} and input ", schema, in);
    List<Future> futures = new ArrayList<>();
    for (Validator validator : validators) {
      if (validator.isAsync()) {
        Future asyncValidate = ((AsyncValidator) validator).validate(in);
        asyncValidate = asyncValidate.recover(t -> {
            if (t instanceof ValidationException) {
              ValidationException e = (ValidationException)t;
              e.setSchema(this);
              e.setScope(this.scope);
              return Future.failedFuture(e);
            } else {
              return Future.failedFuture(NO_MATCH.createException("Error while validating", (Throwable) t, null, in));
            }
        });
        futures.add(asyncValidate);
      } else try {
        ((SyncValidator) validator).validate(in);
      } catch (ValidationException e) {
        e.setSchema(this);
        e.setScope(this.scope);
        return Future.failedFuture(e);
      }
    }
    return CompositeFuture.all(futures).compose(cf -> Future.succeededFuture());
  }

  public ConcurrentSkipListSet<Validator> getValidators() {
    return validators;
  }
}
