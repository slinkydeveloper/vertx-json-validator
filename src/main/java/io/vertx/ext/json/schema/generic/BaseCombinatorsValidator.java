package io.vertx.ext.json.schema.generic;

import io.vertx.ext.json.schema.MutableStateValidator;
import io.vertx.ext.json.schema.Schema;

import java.util.Arrays;
import java.util.List;

public abstract class BaseCombinatorsValidator extends BaseMutableStateValidator {

  protected Schema[] schemas;

  public BaseCombinatorsValidator(MutableStateValidator parent) {
    super(parent);
  }

  @Override
  public boolean calculateIsSync() {
    return Arrays.stream(schemas).map(Schema::isSync).reduce(true, Boolean::logicalAnd);
  }

  void setSchemas(List<Schema> schemas) {
    this.schemas = schemas.toArray(new Schema[schemas.size()]);
    this.initializeIsSync();
  }

}
