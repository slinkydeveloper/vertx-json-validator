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

public class AllOfValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            JsonArray allOfSchemas = schema.getJsonArray("allOf");
            JsonPointer basePointer = JsonPointer.fromURI(scope.toString()).append("allOf");
            Set<Schema> parsedSchemas = new HashSet<>();
            for (int i = 0; i < allOfSchemas.size(); i++) {
                parsedSchemas.add(parser.parse(allOfSchemas.getJsonObject(i), URIUtils.replaceFragment(scope, basePointer.copy().append(Integer.toString(i)).buildURI())));
            }
            return new AllOfValidator(parsedSchemas);
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for allOf keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null allOf keyword");
        }
    }

    class AllOfValidator implements AsyncValidator {

        private Set<Schema> schemas;

        public AllOfValidator(Set<Schema> schemas) {
            this.schemas = schemas;
        }

        @Override
        public Future validate(Object in) {
            return CompositeFuture.all(schemas.stream().map(s -> s.validate(in)).collect(Collectors.toList())).compose(cf -> Future.succeededFuture());
        }
    }

}
