package io.vertx.ext.json.schema.draft7;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;
import io.vertx.ext.json.schema.generic.FutureUtils;

import java.util.stream.Collectors;

public class ContainsValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      return new ContainsValidator(
          parser.parse(schema.getValue("contains"), scope.copy().append("contains"))
      );
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for contains keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null contains keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("contains");
  }

  class ContainsValidator implements AsyncValidator {

    private final Schema schema;

    public ContainsValidator(Schema schema) { this.schema = schema; }

    @Override
    public Future<Void> validate(Object in) {
      if (in instanceof JsonArray){
        if (((JsonArray)in).isEmpty()) return Future.failedFuture(ValidationErrorType.NO_MATCH.createException("provided array should not be empty", "contains", in));
        else return FutureUtils.andThen(
            CompositeFuture.any(
              ((JsonArray) in).stream().map(schema::validate).collect(Collectors.toList())
            ),
            cf -> Future.succeededFuture(),
            err -> Future.failedFuture(ValidationErrorType.NO_MATCH.createException("provided array doesn't contain an element matching the contains schema", err, "contains", in))
        );
      } else return Future.succeededFuture();
    }
  }

}
