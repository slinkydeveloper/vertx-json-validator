package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.pointer.JsonPointer;

import java.net.URI;
import java.util.function.Predicate;

@VertxGen
public interface SchemaParser {

  //TODO add parse from file/url

  /**
   * Build a schema from provided json. This method registers the parsed schema (and relative subschemas) to the schema router
   *
   * @param jsonSchema JSON representing the schema. Must be a JsonObject or a Boolean
   * @param scope Scope of schema. Must be a JSONPointer with absolute URI
   * @param parent Parent schema. Null if there is no direct parent
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  Schema parse(Object jsonSchema, JsonPointer scope, MutableStateValidator parent);

  /**
   * Build a schema without a parent from provided json. This method registers the parsed schema (and relative subschemas) to the schema router
   *
   * @param jsonSchema JSON representing the schema. Must be a JsonObject or a Boolean
   * @param scope Scope of schema. Must be a JSONPointer with absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parse(Object jsonSchema, JsonPointer scope) { return parse(jsonSchema, scope, null); }

  /**
   * Same as {@link SchemaParser#parse(Object, JsonPointer, MutableStateValidator)}
   *
   * @param jsonSchema JSON representing the schema. Must be a JsonObject or a Boolean
   * @param scope Scope of schema. Must be an absolute URI
   * @param parent Parent schema. Null if there is no direct parent
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parse(Object jsonSchema, URI scope, MutableStateValidator parent) { return this.parse(jsonSchema, JsonPointer.fromURI(scope), parent); }

  /**
   * Same as {@link SchemaParser#parse(Object, JsonPointer)}
   *
   * @param jsonSchema JSON representing the schema. Must be a JsonObject or a Boolean
   * @param scope Scope of schema. Must be an absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parse(Object jsonSchema, URI scope) { return parse(jsonSchema, scope, null); }

  /**
   * Build a schema from provided unparsed json. This method registers the parsed schema (and relative subschemas) to the schema router
   *
   * @param unparsedJson Unparsed JSON representing the schema.
   * @param scope Scope of schema. Must be a JSONPointer with absolute URI
   * @param parent Parent schema. Null if there is no direct parent
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  Schema parseSchemaFromString(String unparsedJson, JsonPointer scope, MutableStateValidator parent);

  /**
   * Build a schema without a parent from provided unparsed json. This method registers the parsed schema (and relative subschemas) to the schema router
   *
   * @param unparsedJson Unparsed JSON representing the schema.
   * @param scope Scope of schema. Must be a JSONPointer with absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parseSchemaFromString(String unparsedJson, JsonPointer scope) { return parseSchemaFromString(unparsedJson, scope,  null); };

  /**
   * Same as {@link SchemaParser#parseSchemaFromString(String, JsonPointer, MutableStateValidator)}
   *
   * @param unparsedJson Unparsed JSON representing the schema.
   * @param scope Scope of schema. Must be an absolute URI
   * @param parent Parent schema. Null if there is no direct parent
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parseSchemaFromString(String unparsedJson, URI scope, MutableStateValidator parent) { return this.parseSchemaFromString(unparsedJson, JsonPointer.fromURI(scope), parent); }

  /**
   * Same as {@link SchemaParser#parseSchemaFromString(String, JsonPointer)}
   *
   * @param unparsedJson Unparsed JSON representing the schema.
   * @param scope Scope of schema. Must be an absolute URI
   * @return the schema instance
   * @throws IllegalArgumentException If scope is relative
   * @throws SchemaException If schema is invalid
   */
  default Schema parseSchemaFromString(String unparsedJson, URI scope) { return this.parseSchemaFromString(unparsedJson, JsonPointer.fromURI(scope), null); }

  /**
   * Get schema router registered to this schema parser
   * @return
   */
  SchemaRouter getSchemaRouter();

  //TODO document

  @Fluent
  SchemaParser withValidatorFactory(ValidatorFactory factory);

  @Fluent
  SchemaParser withStringFormatValidator(String formatName, Predicate<String> predicate);
}
