package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaErrorType;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.Validator;

import java.net.URI;
import java.util.concurrent.ConcurrentSkipListSet;

public class RefSchema extends SchemaImpl {

  private static final Logger log = LoggerFactory.getLogger(RefSchema.class);

  private final JsonPointer refPointer;
  private final SchemaParser schemaParser;
  private Schema cachedSchema;

  public RefSchema(JsonObject schema, JsonPointer scope, ConcurrentSkipListSet<Validator> validators, SchemaParser schemaParser) {
    super(schema, scope, validators);
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

  private void removeOverrides() {
    // In draft-6 and openapi the ref schema should not care about other keywords! from draft-8 this function makes sense
//    this.getValidators().removeIf(validator ->
//      ((SchemaImpl)cachedSchema).getValidators().stream().map(v -> validator.getClass().equals(v.getClass())).filter(b -> b).findFirst().orElse(false)
//    );
//    this.getValidators().addAll(((SchemaImpl)this.cachedSchema).getValidators());
    this.getValidators().clear();
    this.getValidators().addAll(((SchemaImpl)this.cachedSchema).getValidators());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Future validate(Object in) {
    if (cachedSchema == null) {
      return schemaParser.getSchemaRouter()
          .resolveRef(refPointer, this.getScope(), schemaParser)
          .compose(s -> {
            if (s == null) return Future.failedFuture(SchemaErrorType.UNABLE_TO_SOLVE_REF.createException(this.getSchema(), "Unable to solve reference " + this.refPointer.buildURI()));
            this.cachedSchema = s;
            if (s instanceof RefSchema) {
              // We need to call solved schema validate to solve upper ref, then we can merge validators
              return s.validate(in).compose(f -> {
                removeOverrides();
                return super.validate(in);
              });
            } else if (BaseSchemaParser.FALSE_SCHEMA == s || BaseSchemaParser.TRUE_SCHEMA == s) {
              return s.validate(in);
            } else {
              if (log.isDebugEnabled()) log.debug("Solved schema {}", s.getScope());
              removeOverrides();
              return super.validate(in);
            }
          });
    } else {
      if (BaseSchemaParser.FALSE_SCHEMA == cachedSchema || BaseSchemaParser.TRUE_SCHEMA == cachedSchema)
        return cachedSchema.validate(in);
      else
        return super.validate(in);
    }
  }
}
