package io.vertx.ext.json.validator;

public interface MutableStateValidator extends AsyncValidator, SyncValidator {

  //TODO comment
  MutableStateValidator getParent();

  //TODO comment
  void triggerUpdateIsSync();

}
