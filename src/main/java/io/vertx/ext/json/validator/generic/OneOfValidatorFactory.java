package io.vertx.ext.json.validator.generic;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.CompositeFutureImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OneOfValidatorFactory implements ValidatorFactory {

    @Override
    public Validator createValidator(JsonObject schema, URI scope, SchemaParser parser) {
        try {
            JsonArray oneOfSchemas = schema.getJsonArray("oneOf");
            if (oneOfSchemas.size() == 0) throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "oneOf must have at least one element");
            JsonPointer basePointer = JsonPointer.fromURI(scope.toString()).append("oneOf");
            Set<Schema> parsedSchemas = new HashSet<>();
            for (int i = 0; i < oneOfSchemas.size(); i++) {
                parsedSchemas.add(parser.parse(oneOfSchemas.getJsonObject(i), URIUtils.replaceFragment(scope, basePointer.copy().append(Integer.toString(i)).buildURI())));
            }
            return new OneOfValidator(parsedSchemas);
        } catch (ClassCastException e) {
            throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for oneOf keyword");
        } catch (NullPointerException e) {
            throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null oneOf keyword");
        }
    }

    @Override
    public boolean canCreateValidator(JsonObject schema) {
        return schema.containsKey("not");
    }

    class OneOfValidator implements AsyncValidator {

        private Set<Schema> schemas;

        public OneOfValidator(Set<Schema> schemas) {
            this.schemas = schemas;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Future validate(Object in) {
            return oneOf(schemas.stream().map(s -> s.validate(in)).collect(Collectors.toList()));
        }
    }

    private static Future oneOf(List<Future<?>> results) {
        final Future res = Future.future();
        final AtomicInteger processed = new AtomicInteger(0);
        final AtomicBoolean atLeastOneOk = new AtomicBoolean(false);
        final int len = results.size();
        for (int i = 0; i < len; i++) {
            results.get(i).setHandler(ar -> {
                int p = processed.incrementAndGet();
                if (ar.succeeded()) {
                    if (atLeastOneOk.get()) res.fail(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO
                    else atLeastOneOk.set(true);
                }
                if (p == len) {
                    if (atLeastOneOk.get()) res.complete();
                    else res.fail(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO
                }
            });
        }
        return res;
    }

}
