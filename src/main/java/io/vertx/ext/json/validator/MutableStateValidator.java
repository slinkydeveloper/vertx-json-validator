package io.vertx.ext.json.validator;

public interface MutableStateValidator extends Validator {

  //TODO comment
  MutableStateValidator getParent();

  //TODO comment
  void triggerUpdateIsSync();

}
