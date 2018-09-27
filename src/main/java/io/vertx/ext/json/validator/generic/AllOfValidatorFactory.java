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

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class AllOfValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonArray allOfSchemas = schema.getJsonArray("allOf");
      if (allOfSchemas.size() == 0)
        throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "allOf must have at least one element");
      JsonPointer basePointer = scope.append("allOf");
      List<Schema> parsedSchemas = new ArrayList<>();
      for (int i = 0; i < allOfSchemas.size(); i++) {
        parsedSchemas.add(parser.parse(allOfSchemas.getValue(i), basePointer.copy().append(Integer.toString(i))));
      }
      return new AllOfValidator(parsedSchemas);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for allOf keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null allOf keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("allOf");
  }

  class AllOfValidator implements AsyncValidator {

    private final Schema[] schemas;

    public AllOfValidator(List<Schema> schemas) {
      this.schemas = schemas.toArray(new Schema[schemas.size()]);
    }

    @Override
    public Future<Void> validate(Object in) {
      return FutureUtils.andThen(
          CompositeFuture.all(Arrays.stream(schemas).map(s -> s.validate(in)).collect(Collectors.toList())),
          res -> Future.succeededFuture(),
          err -> Future.failedFuture(NO_MATCH.createException("allOf subschema don't match", err, "allOf", in)));
    }
  }

}
