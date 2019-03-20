package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.VertxGen;

@VertxGen(concrete = false)
public interface MutableStateValidator extends AsyncValidator, SyncValidator {

  //TODO comment
  MutableStateValidator getParent();

  //TODO comment
  void triggerUpdateIsSync();

}
