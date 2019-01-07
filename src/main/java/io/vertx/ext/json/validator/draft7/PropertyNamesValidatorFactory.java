package io.vertx.ext.json.validator.draft7;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.validator.AsyncValidatorException;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.ValidationErrorType;
import io.vertx.ext.json.validator.ValidationException;
import io.vertx.ext.json.validator.generic.BaseSingleSchemaValidator;
import io.vertx.ext.json.validator.generic.BaseSingleSchemaValidatorFactory;
import io.vertx.ext.json.validator.generic.FutureUtils;

import java.util.stream.Collectors;

public class PropertyNamesValidatorFactory extends BaseSingleSchemaValidatorFactory {

  @Override
  protected BaseSingleSchemaValidator instantiate(MutableStateValidator parent) {
    return new PropertyNamesValidator(parent);
  }

  @Override
  protected String getKeyword() {
    return "propertyNames";
  }

  class PropertyNamesValidator extends BaseSingleSchemaValidator {

    public PropertyNamesValidator(MutableStateValidator parent) {
      super(parent);
    }

    @Override
    public void validateSync(Object in) throws ValidationException, AsyncValidatorException {
      this.checkSync();
      if (in instanceof JsonObject) {
        ((JsonObject) in).getMap().keySet().stream().forEach(schema::validateSync);
      }
    }

    @Override
    public Future<Void> validateAsync(Object in) {
      if (isSync()) return validateSyncAsAsync(in);
      if (in instanceof JsonObject){
        return FutureUtils.andThen(
            CompositeFuture.all(
              ((JsonObject) in).getMap().keySet().stream().map(schema::validateAsync).collect(Collectors.toList())
            ),
            cf -> Future.succeededFuture(),
            err -> Future.failedFuture(ValidationErrorType.NO_MATCH.createException("provided object contains a key not matching the propertyNames schema", err, "propertyNames", in))
        );
      } else return Future.succeededFuture();
    }
  }

}