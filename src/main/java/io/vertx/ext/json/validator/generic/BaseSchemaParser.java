package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.pointer.impl.JsonPointerImpl;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.web.client.WebClient;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseSchemaParser implements SchemaParser {

  private final static Schema TRUE_SCHEMA = (in) -> Future.succeededFuture();
  private final static Schema FALSE_SCHEMA = (in) -> Future.failedFuture(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO

  protected final Object schemaRoot;
  protected final URI baseScope;
  protected final SchemaParserOptions options;
  protected final List<ValidatorFactory> validatorFactories;
  protected final SchemaRouter router;
  protected final WebClient client;
  protected final FileSystem fs;

  protected BaseSchemaParser(Object schemaRoot, URI baseScope, SchemaParserOptions options, SchemaRouter router, WebClient client, FileSystem fs) {
    this.schemaRoot = schemaRoot;
    this.baseScope = baseScope;
    this.options = options;
    this.router = router;
    this.client = client;
    this.fs = fs;
    this.validatorFactories = initValidatorFactories();
    loadOptions();
  }

  @Override
  public SchemaRouter getSchemaRouter() {
    return router;
  }

  @Override
  public Schema parse() {
    return this.parse(schemaRoot, baseScope);
  }

  protected Schema parse(Object schema, URI uri) {
    return this.parse(schema, JsonPointer.fromURI(uri));
  }

  @Override
  public Schema parse(Object schema, JsonPointer scope) {
    if (schema instanceof JsonObject) {
      JsonObject json = (JsonObject) schema;
      ConcurrentSkipListSet<Validator> validators = new ConcurrentSkipListSet<>(ValidatorPriority.VALIDATOR_COMPARATOR);

      Schema s = createSchema(json, scope, validators);
      router.addSchema(s, scope);

      for (ValidatorFactory factory : validatorFactories) {
        if (factory.canCreateValidator(json)) {
          Validator v = factory.createValidator(json, scope.copy(), this);
          if (v != null) validators.add(v);
        }
      }

      return s;
    } else if (schema instanceof Boolean) {
      Schema s = ((Boolean) schema) ? TRUE_SCHEMA : FALSE_SCHEMA;
      router.addSchema(s, scope);
      return s;
    } else
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Schema should be a JsonObject or a Boolean");
  }

  protected Schema createSchema(JsonObject schema, JsonPointer scope, ConcurrentSkipListSet<Validator> validators) {
    if (schema.containsKey("$ref")) return new RefSchema(schema, scope, validators, this);
    else return new SchemaImpl(schema, scope, validators);
  }

  protected abstract List<ValidatorFactory> initValidatorFactories();

  protected void loadOptions() {
    // Load additional validators
    this.validatorFactories.addAll(this.options.getAdditionalValidatorFactories());

    // Load additional string formats
    ValidatorFactory f = validatorFactories
        .stream()
        .filter(factory -> factory instanceof BaseFormatValidatorFactory)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("This json schema version doesn't support format keyword"));
    this.options.getAdditionalStringFormatValidators().forEach(((BaseFormatValidatorFactory) f)::addStringFormatValidator);
  }

  private Schema parseSchemaFromString(String schema, URI uri) {
    String unparsedSchema = schema.trim();
    if ("false".equals(unparsedSchema) || "true".equals(unparsedSchema)) return this.parse(Boolean.parseBoolean(unparsedSchema), uri);
    else return this.parse(new JsonObject(unparsedSchema), uri);
  }

  @Override
  public Future<Schema> solveRef(final JsonPointer pointer, final JsonPointer scope) {
    Schema cachedSchema = getSchemaRouter().resolveCachedSchema(pointer, scope);
    if (cachedSchema == null) {
      URI u = pointer.getURIWithoutFragment();
      Future<Schema> fut = Future.future();
      if ("http".equals(u.getScheme())) {
        client
            .get(u.toString())
            .putHeader(HttpHeaders.ACCEPT.toString(), "application/json, application/schema+json")
            .send(res -> {
              if (res.succeeded() && res.result().statusCode() == 200) {
                parseSchemaFromString(res.result().bodyAsString(), u);
                fut.tryComplete(getSchemaRouter().resolveCachedSchema(pointer, scope));
              } else {
                if (res.failed()) fut.tryFail(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO use fail
                else fut.tryFail(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO wrong status code
              }
            });
      } else {
        URI fileURI = scope.getURIWithoutFragment().resolve(u);
        fs.readFile(fileURI.toString(), res -> {
          if (res.succeeded()) {
            parseSchemaFromString(res.result().toString(), u);
            fut.tryComplete(getSchemaRouter().resolveCachedSchema(pointer, scope));
          } else {
            fut.tryFail(ValidationExceptionFactory.generateNotMatchValidationException(res.cause().toString())); //TODO use fail
          }
        });
      }
      return fut;
    } else return Future.succeededFuture(cachedSchema);
  }
}
