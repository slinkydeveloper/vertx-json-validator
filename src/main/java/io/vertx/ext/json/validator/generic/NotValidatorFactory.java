package io.vertx.ext.json.validator.generic;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NotValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            JsonObject notSchema = schema.getJsonObject("not");
            Schema parsedSchema = parser.parse(notSchema, URIUtils.replaceFragment(scope, JsonPointer.fromURI(scope.toString()).append("not").buildURI()));
            return new NotValidator(parsedSchema);
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for not keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null not keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("not");
    }

    class NotValidator implements AsyncValidator {

        private Schema schema;

        public NotValidator(Schema schema) {
            this.schema = schema;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Future validate(Object in) {
            Future future = Future.future();
            schema.validate(in).setHandler(ar -> {
                if (((AsyncResult)ar).succeeded()) future.fail(ValidationExceptionFactory.generateNotMatchValidationException(""));
                else future.complete();
            });
            return future;
        }
    }

}
