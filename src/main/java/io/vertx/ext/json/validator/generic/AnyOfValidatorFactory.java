package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AnyOfValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            JsonArray anyOfSchemas = schema.getJsonArray("anyOf");
            if (anyOfSchemas.size() == 0) throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "anyOf must have at least one element");
            JsonPointer basePointer = JsonPointer.fromURI(scope.toString()).append("anyOf");
            Set<Schema> parsedSchemas = new HashSet<>();
            for (int i = 0; i < anyOfSchemas.size(); i++) {
                parsedSchemas.add(parser.parse(anyOfSchemas.getJsonObject(i), URIUtils.replaceFragment(scope, basePointer.copy().append(Integer.toString(i)).buildURI())));
            }
            return new AnyOfValidator(parsedSchemas);
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for anyOf keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null anyOf keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("anyOf");
    }

    class AnyOfValidator implements AsyncValidator {

        private Set<Schema> schemas;

        public AnyOfValidator(Set<Schema> schemas) {
            this.schemas = schemas;
        }

        @Override
        public Future validate(Object in) {
            return CompositeFuture.any(schemas.stream().map(s -> s.validate(in)).collect(Collectors.toList())).compose(cf -> Future.succeededFuture());
        }
    }

}
