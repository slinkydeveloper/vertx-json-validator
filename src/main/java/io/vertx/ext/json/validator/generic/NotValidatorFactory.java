package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.NoSyncValidationException;
import io.vertx.ext.json.validator.ValidationException;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class NotValidatorFactory extends BaseSingleSchemaValidatorFactory {

  @Override
  protected BaseSingleSchemaValidator instantiate(MutableStateValidator parent) {
    return new NotValidator(parent);
  }

  @Override
  protected String getKeyword() {
    return "not";
  }

  class NotValidator extends BaseSingleSchemaValidator {

    public NotValidator(MutableStateValidator parent) {
      super(parent);
    }

    private boolean isValidSync(Object in) {
      try {
        schema.validateSync(in);
        return true;
      } catch (ValidationException e) {
        return false;
      }
    }

    @Override
    public void validateSync(Object in) throws ValidationException, NoSyncValidationException {
      this.checkSync();
      if (isValidSync(in)) throw createException("input should be invalid", "not", in);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<Void> validateAsync(Object in) {
      if (isSync()) return validateSyncAsAsync(in);
      return FutureUtils.andThen(
          schema.validateAsync(in),
          res -> Future.failedFuture(createException("input should be invalid", "not", in)),
          err -> Future.succeededFuture()
      );
    }

  }

}
