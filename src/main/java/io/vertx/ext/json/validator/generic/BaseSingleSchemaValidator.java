package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.validator.MutableStateValidator;
import io.vertx.ext.json.validator.Schema;

public abstract class BaseSingleSchemaValidator extends BaseMutableStateValidator {

  protected Schema schema;

  public BaseSingleSchemaValidator(MutableStateValidator parent) {
    super(parent);
  }

  @Override
  public boolean calculateIsSync() {
    return schema.isSync();
  }

  void setSchema(Schema schema) {
    this.schema = schema;
    this.initializeIsSync();
  }

}
