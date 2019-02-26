package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.NoSyncValidationException;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.ValidationException;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class AnyOfValidatorFactory extends BaseCombinatorsValidatorFactory {

  @Override
  BaseCombinatorsValidator instantiate(MutableStateValidator parent) {
    return new AnyOfValidator(parent);
  }

  @Override
  String getKeyword() {
    return "anyOf";
  }

  class AnyOfValidator extends BaseCombinatorsValidator {

    public AnyOfValidator(MutableStateValidator parent) {
      super(parent);
    }

    @Override
    public void validateSync(Object in) throws ValidationException, NoSyncValidationException {
      this.checkSync();
      ValidationException res = null;
      for (Schema s : this.schemas) {
        try {
          s.validateSync(in);
          return;
        } catch (ValidationException e) {
          res = e;
        }
      }
      throw res;
    }

    @Override
    public Future<Void> validateAsync(Object in) {
      if (isSync()) return validateSyncAsAsync(in);
      return FutureUtils.andThen(
          CompositeFuture.any(Arrays.stream(this.schemas).map(s -> s.validateAsync(in)).collect(Collectors.toList())),
              res -> Future.succeededFuture(),
              err -> Future.failedFuture(createException("anyOf subschemas don't match", "anyOf", in, err))
      );
    }

  }

}
