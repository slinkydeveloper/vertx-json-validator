package io.vertx.ext.json.validator.draft7;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.NoSyncValidationException;
import io.vertx.ext.json.validator.ValidationException;
import io.vertx.ext.json.validator.generic.BaseSingleSchemaValidator;
import io.vertx.ext.json.validator.generic.BaseSingleSchemaValidatorFactory;
import io.vertx.ext.json.validator.generic.FutureUtils;

import java.util.stream.Collectors;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class ContainsValidatorFactory extends BaseSingleSchemaValidatorFactory {

  @Override
  protected BaseSingleSchemaValidator instantiate(MutableStateValidator parent) {
    return new ContainsValidator(parent);
  }

  @Override
  protected String getKeyword() {
    return "contains";
  }

  class ContainsValidator extends BaseSingleSchemaValidator {

    public ContainsValidator(MutableStateValidator parent) {
      super(parent);
    }

    @Override
    public Future<Void> validateAsync(Object in) {
      if (isSync()) return validateSyncAsAsync(in);
      if (in instanceof JsonArray){
        if (((JsonArray)in).isEmpty()) return Future.failedFuture(createException("provided array should not be empty", "contains", in));
        else return FutureUtils.andThen(
            CompositeFuture.any(
              ((JsonArray) in).stream().map(schema::validateAsync).collect(Collectors.toList())
            ),
            cf -> Future.succeededFuture(),
            err -> Future.failedFuture(createException("provided array doesn't contain an element matching the contains schema", "contains", in, err))
        );
      } else return Future.succeededFuture();
    }

    @Override
    public void validateSync(Object in) throws ValidationException, NoSyncValidationException {
      this.checkSync();
      ValidationException t = null;
      if (in instanceof JsonArray){
        for (Object item : (JsonArray) in) {
          try {
            schema.validateSync(item);
            return;
          } catch (ValidationException e) {
            t = e;
          }
        }
        throw createException("provided array doesn't contain an element matching the contains schema", "contains", in, t);
      }
    }

  }

}
