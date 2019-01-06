package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.json.validator.AsyncValidatorException;
import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class ItemsValidatorFactory extends BaseSingleSchemaValidatorFactory {

  @Override
  protected BaseSingleSchemaValidator instantiate(MutableStateValidator parent) {
    return new ItemsValidator(parent);
  }

  @Override
  protected String getKeyword() {
    return "items";
  }

  class ItemsValidator extends BaseSingleSchemaValidator {

    public ItemsValidator(MutableStateValidator parent) {
      super(parent);
    }

    @Override
    public void validateSync(Object in) throws ValidationException, AsyncValidatorException {
      this.checkSync();
      if (in instanceof JsonArray) {
        JsonArray arr = (JsonArray) in;
        arr.forEach(schema::validateSync);
      }
    }

    @Override
    public Future<Void> validateAsync(Object in) {
      if (isSync()) return validateSyncAsAsync(in);
      if (in instanceof JsonArray) {
        JsonArray arr = (JsonArray) in;
        List<Future> futs = new ArrayList<>();
        for (Object v : arr) {
          Future<Void> f = schema.validateAsync(v);
          if (f.isComplete()) {
            if (f.failed()) return Future.failedFuture(f.cause());
          } else {
            futs.add(f);
          }
        }
        if (futs.isEmpty())
          return Future.succeededFuture();
        else
          return CompositeFuture.all(futs).compose(cf -> Future.succeededFuture());
      } else return Future.succeededFuture();
    }
  }
}
