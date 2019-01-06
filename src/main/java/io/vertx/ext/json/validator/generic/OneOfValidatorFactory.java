package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.ext.json.validator.AsyncValidatorException;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.ValidationException;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class OneOfValidatorFactory extends BaseCombinatorsValidatorFactory {

  @Override
  BaseCombinatorsValidator instantiate(MutableStateValidator parent) {
    return new OneOfValidator(parent);
  }

  @Override
  String getKeyword() {
    return "oneOf";
  }

  class OneOfValidator extends BaseCombinatorsValidator {

    public OneOfValidator(MutableStateValidator parent) {
      super(parent);
    }

    private boolean isValidSync(Schema schema, Object in) {
      try {
        schema.validateSync(in);
        return true;
      } catch (ValidationException e) {
        return false;
      }
    }

    @Override
    public void validateSync(Object in) throws ValidationException, AsyncValidatorException {
      this.checkSync();
      long validCount = Arrays.stream(schemas).map(s -> isValidSync(s, in)).filter(b -> b.equals(true)).count();
      if (validCount > 1) throw NO_MATCH.createException("More than one schema valid", "oneOf", in);
      else if (validCount == 0) throw NO_MATCH.createException("No schema matches", "oneOf", in);
    }

    @Override
    public Future<Void> validateAsync(Object in) {
      if (isSync()) return validateSyncAsAsync(in);
      return FutureUtils.oneOf(Arrays.stream(schemas).map(s -> s.validateAsync(in)).collect(Collectors.toList()));
    }
  }

}
