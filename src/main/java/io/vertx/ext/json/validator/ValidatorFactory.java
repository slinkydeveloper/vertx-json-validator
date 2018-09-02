package io.vertx.ext.json.validator;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;

import java.net.URI;

public interface ValidatorFactory {
    /**
     * This method can return null!
     *
     * @param schema
     * @param scope
     * @param parser
     * @return
     */
    Validator createValidator(JsonObject schema, URI scope, SchemaParser parser);

    //TODO change name in something better...
    boolean canCreateValidator(JsonObject schema);
}
