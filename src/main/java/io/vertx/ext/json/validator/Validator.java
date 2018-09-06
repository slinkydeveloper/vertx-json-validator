package io.vertx.ext.json.validator;

public interface Validator {
  boolean isAsync();

  ValidatorPriority getPriority();
}
