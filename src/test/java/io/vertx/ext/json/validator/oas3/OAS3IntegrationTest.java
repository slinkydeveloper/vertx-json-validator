package io.vertx.ext.json.validator.oas3;

import io.vertx.ext.json.validator.BaseIntegrationTest;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.SchemaParserOptions;
import io.vertx.ext.json.validator.generic.SchemaRouterImpl;
import io.vertx.ext.json.validator.openapi3.OpenAPI3SchemaParser;
import org.assertj.core.util.Lists;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;
import java.nio.file.Paths;
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
//        "discriminator",
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
    SchemaParser parser = OpenAPI3SchemaParser.create(new SchemaParserOptions(), new SchemaRouterImpl(vertx.createHttpClient(), vertx.fileSystem()));
    return parser.parse(schema, Paths.get(this.getSchemasPath() + "/" + testFileName + ".json").toAbsolutePath().toUri());
  }

  @Override
  public String getSchemasPath() {
    return "src/test/resources/tck/openapi3";
  }

  @Override
  public String getRemotesPath() {
    return "src/test/resources/tck/openapi3/remotes";
  }
}
