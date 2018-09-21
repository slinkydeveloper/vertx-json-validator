package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OneOfValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonArray oneOfSchemas = schema.getJsonArray("oneOf");
      if (oneOfSchemas.size() == 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "oneOf must have at least one element");
      JsonPointer basePointer = scope.append("oneOf");
      List<Schema> parsedSchemas = new ArrayList<>();
      for (int i = 0; i < oneOfSchemas.size(); i++) {
        parsedSchemas.add(parser.parse(oneOfSchemas.getJsonObject(i), basePointer.copy().append(Integer.toString(i))));
      }
      return new OneOfValidator(parsedSchemas);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for oneOf keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null oneOf keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("oneOf");
  }

  class OneOfValidator implements AsyncValidator {

    private Schema[] schemas;

    public OneOfValidator(List<Schema> schemas) {
      this.schemas = schemas.toArray(new Schema[schemas.size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future validate(Object in) {
      return FutureUtils.oneOf(Arrays.stream(schemas).map(s -> s.validate(in)).map(f -> (Future<Object>)f).collect(Collectors.toList()));
    }
  }

}
