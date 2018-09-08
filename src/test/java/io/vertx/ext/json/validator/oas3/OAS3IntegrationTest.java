package io.vertx.ext.json.validator.oas3;

import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaParserOptions;
import io.vertx.ext.json.validator.generic.SchemaRouterImpl;
import io.vertx.ext.json.validator.openapi3.OpenAPI3SchemaParser;
import io.vertx.ext.json.validator.BaseIntegrationTest;
import io.vertx.ext.web.client.WebClient;
import org.assertj.core.util.Lists;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OAS3IntegrationTest extends BaseIntegrationTest {
  public OAS3IntegrationTest(Object testName, Object testFileName, Object testObject) {
    super(testName, testFileName, testObject);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() throws Exception {
    List<String> tests = Lists.newArrayList(
        "additionalProperties",
        "allOf",
        "anyOf",
        "discriminator",
        "enum",
        "exclusiveMaximum",
        "exclusiveMinimum",
        "format",
        "items",
        "maximum",
        "maxItems",
        "maxLength",
        "maxProperties",
        "minimum",
        "minItems",
        "minLength",
        "minProperties",
        "multipleOf",
        "not",
        "nullable",
        "oneOf",
        "pattern",
        "properties",
        "ref",
        "refRemote",
        "required",
        "type",
        "uniqueItems"
    );
    return BaseIntegrationTest.buildParameters(tests);
  }


  @Override
  public Schema buildSchemaFunction(Object schema) throws URISyntaxException {
    return OpenAPI3SchemaParser.create(schema, URI.create("./" + this.testFileName + "/" + testFileName + ".json"), new SchemaParserOptions(), new SchemaRouterImpl(), WebClient.create(vertx), vertx.fileSystem()).parse();
  }

  @Override
  public String getSchemasPath() {
    return "src/test/resources/tck/openapi3";
  }
}
