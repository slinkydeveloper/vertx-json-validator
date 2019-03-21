package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.json.pointer.JsonPointer;

import java.net.URI;

@VertxGen
public interface SchemaParser {

  /**
   * Build a schema from provided json. This method registers the parsed schema (and relative subschemas) to the schema router
   *
   * @param jsonSchema JSON representing the schema. Must be a JsonObject or a Boolean
   * @param scope Scope of schema. Must be a JSONPointer with absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  Schema parse(Object jsonSchema, JsonPointer scope);

  /**
   * Same as {@link SchemaParser#parse(Object, JsonPointer)}
   *
   * @param jsonSchema JSON representing the schema. Must be a JsonObject or a Boolean
   * @param scope Scope of schema. Must be an absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parse(Object jsonSchema, URI scope) { return this.parse(jsonSchema, JsonPointer.fromURI(scope)); }

  /**
   * Build a schema from provided unparsed json. This method registers the parsed schema (and relative subschemas) to the schema router
   *
   * @param unparsedJson Unparsed JSON representing the schema.
   * @param scope Scope of schema. Must be a JSONPointer with absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  Schema parseSchemaFromString(String unparsedJson, JsonPointer scope);

  /**
   * Same as {@link SchemaParser#parseSchemaFromString(String, JsonPointer)}
   *
   * @param unparsedJson Unparsed JSON representing the schema.
   * @param scope Scope of schema. Must be an absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parseSchemaFromString(String unparsedJson, URI scope) { return this.parseSchemaFromString(unparsedJson, JsonPointer.fromURI(scope)); }

  /**
   * Get schema router registered to this schema parser
   * @return
   */
  SchemaRouter getSchemaRouter();
}
