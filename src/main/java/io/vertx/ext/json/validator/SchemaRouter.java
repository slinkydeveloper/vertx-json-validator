package io.vertx.ext.json.validator;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.generic.SchemaRouterImpl;

public interface SchemaRouter {

  /**
   * Resolve cached schema based on refPointer. If a schema isn't cached, it returns null
   *
   * @param refPointer
   * @param schemaScope
   * @return
   */
  Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer schemaScope);

  /**
   * Resolve $ref. <br/>
   * This method tries to resolve schema from local cache. If it's not found, it solve external references.
   * It can solve external references on filesystem and remote references using http.
   * When you pass a relative reference without protocol, it tries to infer the absolute path from scope and cached schemas <br/>
   * Returns a future that can contain Schema or be null or can fail with a {@link SchemaException} or an {@link IllegalArgumentException}
   *
   * @param pointer
   * @param scope
   * @param schemaParser
   * @return
   */
  Future<Schema> resolveRef(JsonPointer pointer, JsonPointer scope, SchemaParser schemaParser);

  /**
   * Add a parsed schema to local cache
   *
   * @param schema
   * @param actualPointer
   */
  void addSchema(Schema schema, JsonPointer actualPointer);

  static SchemaRouter create(Vertx vertx) {
    return new SchemaRouterImpl(vertx.createHttpClient(), vertx.fileSystem());
  }

  static SchemaRouter create(HttpClient client, FileSystem fs) {
    return new SchemaRouterImpl(client, fs);
  }

}
