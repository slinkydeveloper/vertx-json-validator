package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.generic.SchemaRouterImpl;

import java.util.List;

@VertxGen
public interface SchemaRouter {

  /**
   * Resolve cached schema based on refPointer. If a schema isn't cached, it returns null
   *
   * @param refPointer
   * @param schemaScope
   * @param parser
   * @return
   * @throws SchemaException If was found an unparsed schema that is an invalid json schema
   */
  @Nullable Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer schemaScope, SchemaParser parser) throws SchemaException;

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
   *  @param schema
   *
   */
  void addSchema(Schema schema);

  List<Schema> registeredSchemas();

  Future<Schema> solveAllSchemaReferences(Schema schema);

  static SchemaRouter create(Vertx vertx, SchemaRouterOptions schemaRouterOptions) {
    return new SchemaRouterImpl(vertx.createHttpClient(), vertx.fileSystem(), schemaRouterOptions);
  }

  static SchemaRouter create(HttpClient client, FileSystem fs, SchemaRouterOptions schemaRouterOptions) {
    return new SchemaRouterImpl(client, fs, schemaRouterOptions);
  }

}
