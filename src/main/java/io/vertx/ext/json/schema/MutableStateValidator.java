package io.vertx.ext.json.schema;

public interface MutableStateValidator extends AsyncValidator, SyncValidator {

  //TODO comment
  MutableStateValidator getParent();

  //TODO comment
  void triggerUpdateIsSync();

}
