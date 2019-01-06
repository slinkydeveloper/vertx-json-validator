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
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class SchemaImpl extends BaseMutableStateValidator implements Schema {

  private static final Logger log = LoggerFactory.getLogger(SchemaImpl.class);

  private final JsonObject schema;
  private final JsonPointer scope;
  private ConcurrentSkipListSet<Validator> validators;

  public SchemaImpl(JsonObject schema, JsonPointer scope, MutableStateValidator parent) {
    super(parent);
    this.schema = schema;
    this.scope = scope;
  }

  @Override
  public JsonPointer getScope() {
    return scope;
  }

  public JsonObject getSchema() {
    return schema;
  }

  @Override
  public Future<Void> validateAsync(Object in) {
    if (log.isDebugEnabled()) log.debug("Starting async validation for schema {} and input {}", schema, in);
    if (isSync()) return validateSyncAsAsync(in);

    List<Future> futures = new ArrayList<>();
    for (Validator validator : validators) {
      if (!validator.isSync()) {
        Future<Void> asyncValidate = validator.validateAsync(in);
        asyncValidate = asyncValidate.recover(t -> fillException(t, in));
        futures.add(asyncValidate);
      } else try {
        validator.validateSync(in);
      } catch (ValidationException e) {
        e.setSchema(this);
        e.setScope(this.scope);
        return Future.failedFuture(e);
      }
    }
    if (!futures.isEmpty()) {
      return CompositeFuture.all(futures).compose(cf -> Future.succeededFuture());
    } else {
      return Future.succeededFuture();
    }
  }

  @Override
  public void validateSync(Object in) throws ValidationException, AsyncValidatorException {
    if (log.isDebugEnabled()) log.debug("Starting sync validation for schema {} and input {}", schema, in);
    this.checkSync();
    for (Validator validator : validators) {
      try {
        validator.validateSync(in);
      } catch (ValidationException e) {
        e.setSchema(this);
        e.setScope(this.scope);
        throw e;
      }
    }
  }

  @Override
  public boolean calculateIsSync() {
    return validators.stream().map(Validator::isSync).reduce(true, Boolean::logicalAnd);
  }

  public Set<Validator> getValidators() {
    return validators;
  }

  public void setValidators(ConcurrentSkipListSet<Validator> validators) {
    this.validators = validators;
    this.initializeIsSync();
  }

  private Future<Void> fillException(Throwable e, Object in) {
    if (e instanceof ValidationException) {
      ValidationException ve = (ValidationException) e;
      ve.setSchema(this);
      ve.setScope(this.scope);
      return Future.failedFuture(ve);
    } else {
      return Future.failedFuture(NO_MATCH.createException("Error while validating", (Throwable) e, null, in));
    }
  }
}
