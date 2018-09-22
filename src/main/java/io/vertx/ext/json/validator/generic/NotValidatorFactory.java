package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class NotValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Object notSchema = schema.getValue("not");
      Schema parsedSchema = parser.parse(notSchema, scope.append("not"));
      return new NotValidator(parsedSchema);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for not keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null not keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("not");
  }

  class NotValidator implements AsyncValidator {

    private Schema schema;

    public NotValidator(Schema schema) {
      this.schema = schema;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future validate(Object in) {
      return FutureUtils.andThen(
          schema.validate(in),
          res -> Future.failedFuture(NO_MATCH.createException("input should be invalid", "not", in)),
          err -> Future.succeededFuture()
      );
    }
  }

}
