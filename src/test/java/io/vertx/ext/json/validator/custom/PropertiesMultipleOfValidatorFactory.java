package io.vertx.ext.json.validator.custom;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.BaseSyncValidator;

import static io.vertx.ext.json.validator.ValidationException.createException;

public class PropertiesMultipleOfValidatorFactory implements ValidatorFactory {
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser, MutableStateValidator parent) {
    try {
      Number multipleOf = (Number) schema.getValue("propertiesMultipleOf");
      return new PropertiesMultipleOfValidator(multipleOf.intValue());
    } catch (ClassCastException e) {
      throw new SchemaException(schema, "Wrong type for propertiesMultipleOf keyword", e);
    } catch (NullPointerException e) {
      throw new SchemaException(schema, "Null propertiesMultipleOf keyword", e);
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("propertiesMultipleOf");
  }

  private class PropertiesMultipleOfValidator extends BaseSyncValidator {

    private int multipleOf;

    public PropertiesMultipleOfValidator(int multipleOf) {
      this.multipleOf = multipleOf;
    }

    @Override
    public void validateSync(Object in) throws ValidationException, NoSyncValidationException {
      if (in instanceof JsonObject) {
        if (((JsonObject)in).size() % multipleOf != 0)
          throw createException("The provided object size is not a multiple of " + multipleOf, "propertiesMultipleOf", in);
      }
    }
  }
}
