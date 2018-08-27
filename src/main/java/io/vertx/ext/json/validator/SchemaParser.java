package io.vertx.ext.json.validator;

import java.net.URI;
import java.util.Map;

public interface SchemaParser {

    Schema parse();

    SchemaRouter getSchemaRouter();

}
