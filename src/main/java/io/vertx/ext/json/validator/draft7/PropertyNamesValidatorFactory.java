package io.vertx.ext.json.validator.draft7;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.FutureUtils;

import java.util.stream.Collectors;

public class PropertyNamesValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      return new PropertyNamesValidator(
          parser.parse(schema.getValue("propertyNames"), scope.copy().append("propertyNames"))
      );
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for propertyNames keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null propertyNames keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("propertyNames");
  }

  class PropertyNamesValidator implements AsyncValidator {

    private final Schema schema;

    public PropertyNamesValidator(Schema schema) { this.schema = schema; }

    @Override
    public Future validate(Object in) {
      if (in instanceof JsonObject){
        return FutureUtils.andThen(
            CompositeFuture.all(
              ((JsonObject) in).getMap().keySet().stream().map(schema::validate).collect(Collectors.toList())
            ),
            cf -> Future.succeededFuture(),
            err -> Future.failedFuture(ValidationErrorType.NO_MATCH.createException("provided object contains a key not matching the propertyNames schema", err, "propertyNames", in))
        );
      } else return Future.succeededFuture();
    }
  }

}
