# Vert.x JSON Validator Proof of Concept

This is a proof of concept for building a json validator reusing actual logic included in vertx-web-api-contract

This validator should be:

* Fail fast
* Keyword based
* Should support both OpenAPI 3 and draft-8
* Should support both sync and async validation
* Don't apply default values
* Number formats are ignored