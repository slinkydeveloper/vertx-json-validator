# Vert.x JSON Validator

This validator should be:

* Fail fast
* Keyword based
* Should support both OpenAPI 3 and draft-7
* Should support both sync and async validation
* Don't apply default values
* Number formats are ignored

## Architecture

* `SchemaParser`: Parses the schemas. It can be used to parse various schemas. Every JSON Schema dialect/version has one
* `SchemaRouter`: Contains a cache of parsed schemas and resolve cached/local/external `$ref`. You can share it across various `SchemaParser`
* `Validator`: Contains validation logic for single/multiple keyword(s)
* `Schema`: Represents a schema and contains `Validator` instances
* `ValidatorFactory`: Represents a factory for a `Validator`

## How to use
````java
SchemaRouter router = SchemaRouter.create(vertx)
SchemaParserOptions options = new SchemaParserOptions();
SchemaParser parser = Draft7SchemaParser.create(options, router);
Schema schema = parser.parse(schema, scope);
Future validationResult = schema.validate(objectToValidate);
````

Or shorthand

```java
Schema schema = Draft7SchemaParser.parse(vertx, schema, scope);
Future validationResult = schema.validate(objectToValidate);
```

## Extend the validator
To support custom keywords, you can create a new `ValidatorFactory` and register to a `SchemaParser` with `SchemaParserOptions.putAdditionalValidatorFactory()`

## TODO

* Align logging to vertx official packages
* Write documentation
* Javadocs of io.vertx.ext.json interfaces
* Enable vertx codegen
* Schema keyword