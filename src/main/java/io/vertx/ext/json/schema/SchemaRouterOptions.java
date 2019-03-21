package io.vertx.ext.json.schema;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

@VertxGen
@DataObject(generateConverter = true)
public class SchemaRouterOptions {

  private Map<String, String> authQueryParams;
  private Map<String, String> authHeaders;

  public SchemaRouterOptions() {
    authHeaders = new HashMap<>();
    authQueryParams = new HashMap<>();
  }

  public SchemaRouterOptions(JsonObject obj) {

  }

  public JsonObject toJson() {
    return null;
  }

  @Fluent
  public SchemaRouterOptions putAuthHeader(String headerName, String headerValue) {
    authHeaders.put(headerName, headerValue);
    return this;
  }

  @Fluent
  public SchemaRouterOptions putAuthQueryParam(String queryParamName, String queryParamValue) {
    authQueryParams.put(queryParamName, queryParamValue);
    return this;
  }

  public Map<String, String> getAuthQueryParams() {
    return authQueryParams;
  }

  public Map<String, String> getAuthHeaders() {
    return authHeaders;
  }
}
