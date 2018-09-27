package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnyOfValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonArray anyOfSchemas = schema.getJsonArray("anyOf");
      if (anyOfSchemas.size() == 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "anyOf must have at least one element");
      JsonPointer basePointer = scope.append("anyOf");
      List<Schema> parsedSchemas = new ArrayList<>();
      for (int i = 0; i < anyOfSchemas.size(); i++) {
        parsedSchemas.add(parser.parse(anyOfSchemas.getValue(i), basePointer.copy().append(Integer.toString(i))));
      }
      return new AnyOfValidator(parsedSchemas);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for anyOf keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null anyOf keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("anyOf");
  }

  class AnyOfValidator implements AsyncValidator {

    private final Schema[] schemas;

    public AnyOfValidator(List<Schema> schemas) {
      this.schemas = schemas.toArray(new Schema[schemas.size()]);
    }

    @Override
    public Future<Void> validate(Object in) {
      return CompositeFuture.any(
          Arrays.stream(this.schemas)
              .map(s -> s.validate(in))
              .collect(Collectors.toList())
      ).compose(cf -> Future.succeededFuture());
    }
  }

}
