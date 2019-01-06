package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.ext.json.validator.AsyncValidatorException;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.ValidationException;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    public void validateSync(Object in) throws ValidationException, AsyncValidatorException {
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
      return CompositeFuture.any(
          Arrays.stream(this.schemas)
              .map(s -> s.validateAsync(in))
              .collect(Collectors.toList())
      ).compose(cf -> Future.succeededFuture());
    }

  }

}
