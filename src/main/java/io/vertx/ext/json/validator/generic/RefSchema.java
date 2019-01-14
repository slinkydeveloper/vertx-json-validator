package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static io.vertx.ext.json.validator.ValidationErrorType.REF_ERROR;

public class RefSchema extends SchemaImpl {

  private static final Logger log = LoggerFactory.getLogger(RefSchema.class);

  private final JsonPointer refPointer;
  private final SchemaParser schemaParser;
  protected Schema cachedSchema;

  public RefSchema(JsonObject schema, JsonPointer scope, SchemaParser schemaParser, MutableStateValidator parent) {
    super(schema, scope, parent);
    this.schemaParser = schemaParser;
    try {
      String unparsedUri = schema.getString("$ref");
      refPointer = JsonPointer.fromURI(URI.create(unparsedUri));
      if (log.isDebugEnabled()) log.debug("Parsed {} ref for schema {}", refPointer, schema);
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null $ref keyword");
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for $ref keyword");
    } catch (IllegalArgumentException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "$ref URI is invalid: " + e.getMessage());
    }
  }

  private synchronized void registerCachedSchema(Schema s) {
    this.cachedSchema = s;
    if (s instanceof SchemaImpl)
      ((SchemaImpl)s).registerReferredSchema(this);
  }

  public synchronized void prePropagateSyncState() {
    isSync.set(true);
    if (getParent() != null)
      getParent().triggerUpdateIsSync();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Future<Void> validateAsync(Object in) {
    if (isSync()) return validateSyncAsAsync(in);
    if (cachedSchema == null) {
      return FutureUtils.andThen(
          schemaParser.getSchemaRouter().resolveRef(refPointer, this.getScope(), schemaParser),
          s -> {
            if (s == null) return Future.failedFuture(REF_ERROR.createException("Cannot resolve reference " + this.refPointer.buildURI(), "$ref", in));
            registerCachedSchema(s);
            if (log.isDebugEnabled()) log.debug("Solved schema {}", s.getScope());
            if (s instanceof RefSchema) {
              // We need to call solved schema validateAsync to solve upper ref, then we can merge validators
              return s.validateAsync(in).compose(f -> {
                  this.triggerUpdateIsSync();
                  return Future.succeededFuture();
              });
            } else {
              this.triggerUpdateIsSync();
              return s.validateAsync(in);
            }
          },
          err -> Future.failedFuture(REF_ERROR.createException("Error while resolving reference " + this.refPointer.buildURI(), err, "$ref", in))
          );
    } else {
      return cachedSchema.validateAsync(in);
    }
  }

  @Override
  public void validateSync(Object in) throws ValidationException {
    this.checkSync();
    if (cachedSchema == null) {
        Schema s = schemaParser.getSchemaRouter().resolveCachedSchema(refPointer, this.getScope(), schemaParser);
        if (s == null) throw REF_ERROR.createException("Cannot resolve reference " + this.refPointer.buildURI() + " SYNCHRONOUSLY. Maybe this is a remote reference?", "$ref", in);
        registerCachedSchema(s);
        if (!s.isSync()) throw new NoSyncValidationException();
        if (s instanceof RefSchema) {
          // We need to call solved schema validateSync to solve upper ref, then we can merge validators
          s.validateSync(in);
          this.triggerUpdateIsSync();
        } else {
          if (log.isDebugEnabled()) log.debug("Solved schema {}", s.getScope());
          cachedSchema.validateSync(in);
          this.triggerUpdateIsSync();
        }
    } else {
      cachedSchema.validateSync(in);
    }
  }

  @Override
  public boolean calculateIsSync() {
    //return cachedSchema != null && (cachedSchema.isSync() || ((SchemaRouterImpl)schemaParser.getSchemaRouter()).pleaseRunThisShit(this, refPointer, getScope()));
    return cachedSchema != null && cachedSchema.isSync(); //TODO?!
  }

  @Override
  protected void initializeIsSync() {
    // A local pointer could reefer to an asynchronous schema!
//    isSync.set(
//        refPointer.isLocalPointer() &&
//            JsonPointer.mergeBaseURIAndJsonPointer(this.getScope().getURIWithoutFragment(), this.refPointer).isParent(this.getScope())
//    );
    isSync.set(false);
    log.debug("Initialized sync state to false");
  }

  //todo protected and move call to schema parser
  public synchronized void trySyncSolveSchema() {
    if (cachedSchema == null) {
      Schema s = schemaParser.getSchemaRouter().resolveCachedSchema(refPointer, this.getScope(), schemaParser);
      if (s != null) {
        registerCachedSchema(s);
        log.info("RefSchema presolved ref {} with schema {}", refPointer, this.getSchema());
        if (s instanceof RefSchema) {
          ((RefSchema)s).trySyncSolveSchema();
        }
        this.triggerUpdateIsSync();
        log.info("This schema sync state: {}, Resolved schema sync state {}", isSync(), s.isSync());
      }
    }
  }

  public synchronized Future<Schema> tryAsyncSolveSchema() {
    if (cachedSchema == null) {
      return FutureUtils.andThen(
          schemaParser.getSchemaRouter().resolveRef(refPointer, this.getScope(), schemaParser),
          s -> {
            if (s == null) return Future.failedFuture(REF_ERROR.createException("Cannot resolve reference " + this.refPointer.buildURI(), "$ref", null));
            registerCachedSchema(s);
            if (log.isDebugEnabled()) log.debug("Solved schema {}", s.getScope());
            if (s instanceof RefSchema) {
              // We need to call solved schema validateAsync to solve upper ref, then we can merge validators
              return ((RefSchema) s).tryAsyncSolveSchema().map(cachedSchema);
            }
            this.triggerUpdateIsSync();
            return Future.succeededFuture(cachedSchema);
          },
          err -> Future.failedFuture(REF_ERROR.createException("Error while resolving reference " + this.refPointer.buildURI(), err, "$ref", null))
      );
    } else return Future.succeededFuture(cachedSchema);
  }

  protected void setIsSync(boolean s) {
    isSync.set(s);
    if (getParent() != null)
      getParent().triggerUpdateIsSync();
  }
}
