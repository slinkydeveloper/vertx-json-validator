package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.net.URI;

import static io.vertx.ext.json.validator.ValidationErrorType.REF_ERROR;

public class RefSchema extends SchemaImpl {

  private static final Logger log = LoggerFactory.getLogger(RefSchema.class);

  private final JsonPointer refPointer;
  private final SchemaParser schemaParser;
  private Schema cachedSchema;

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

  private synchronized void removeOverrides() {
    // In draft-6 and openapi the ref schema should not care about other keywords! from draft-8 this function makes sense
//    this.getValidators().removeIf(validator ->
//      ((SchemaImpl)cachedSchema).getValidators().stream().map(v -> validator.getClass().equals(v.getClass())).filter(b -> b).findFirst().orElse(false)
//    );
//    this.getValidators().addAll(((SchemaImpl)this.cachedSchema).getValidators());
    this.getValidators().clear();
    this.getValidators().addAll(((SchemaImpl)this.cachedSchema).getValidators());
  }

  private synchronized void registerCachedSchema(Schema s) {
    this.cachedSchema = s;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Future<Void> validateAsync(Object in) {
    if (cachedSchema == null) {
      return FutureUtils.andThen(
          schemaParser.getSchemaRouter().resolveRef(refPointer, this.getScope(), schemaParser),
          s -> {
            if (s == null) return Future.failedFuture(REF_ERROR.createException("Cannot resolve reference " + this.refPointer.buildURI(), "$ref", in));
            registerCachedSchema(s);
            triggerUpdateIsSync();
            if (s instanceof RefSchema) {
              // We need to call solved schema validateAsync to solve upper ref, then we can merge validators
              return s.validateAsync(in).compose(f -> {
                removeOverrides();
                return super.validateAsync(in);
              });
            } else if (FalseSchema.getInstance() == s || TrueSchema.getInstance() == s) {
              return s.validateAsync(in);
            } else {
              if (log.isDebugEnabled()) log.debug("Solved schema {}", s.getScope());
              removeOverrides();
              return super.validateAsync(in);
            }
          },
          err -> Future.failedFuture(REF_ERROR.createException("Error while resolving reference " + this.refPointer.buildURI(), err, "$ref", in))
          );
    } else {
      if (FalseSchema.getInstance() == cachedSchema || TrueSchema.getInstance() == cachedSchema)
        return cachedSchema.validateAsync(in);
      else
        return super.validateAsync(in);
    }
  }

  @Override
  public void validateSync(Object in) throws ValidationException {
    cachedSchema.validateSync(in);
  }

  @Override
  public boolean calculateIsSync() {
    return cachedSchema != null && cachedSchema.isSync();
  }
}
