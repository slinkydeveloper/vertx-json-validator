package io.vertx.ext.json.validator.draft7;

import io.vertx.ext.json.validator.BaseIntegrationTest;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.SchemaParserOptions;
import io.vertx.ext.json.validator.generic.SchemaRouterImpl;
import org.assertj.core.util.Lists;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class Draft7IntegrationTest extends BaseIntegrationTest {
  public Draft7IntegrationTest(Object testName, Object testFileName, Object testObject) {
    super(testName, testFileName, testObject);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() throws Exception {
    List<String> tests = Lists.newArrayList(
        "additionalItems",
        "additionalProperties",
        "allOf",
        "anyOf",
        "boolean_schema",
        "const",
        "contains",
        "definitions",
        "dependencies",
        "enum",
        "exclusiveMaximum",
        "exclusiveMinimum",
        "if-then-else",
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
        "oneOf",
        "pattern",
        "patternProperties",
        "properties",
        "propertyNames",
        "ref",
        "refRemote",
        "required",
        "type",
        "uniqueItems"
    );
    return BaseIntegrationTest.buildParameters(tests, Paths.get("src", "test", "resources", "tck", "draft7"));
  }


  @Override
  public Schema buildSchemaFunction(Object schema) throws URISyntaxException {
    SchemaParser parser = Draft7SchemaParser.create(new SchemaParserOptions(), new SchemaRouterImpl(vertx.createHttpClient(), vertx.fileSystem()));
    return parser.parse(schema, Paths.get(this.getSchemasPath() + "/" + testFileName + ".json").toAbsolutePath().toUri());
  }

  @Override
  public String getSchemasPath() {
    return "src/test/resources/tck/draft7";
  }

  @Override
  public String getRemotesPath() {
    return "src/test/resources/tck/draft7/remotes";
  }
}
