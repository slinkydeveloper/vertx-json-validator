package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.List;

public class ItemsValidatorFactory implements ValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Object itemsSchema = schema.getValue("items");
      Schema parsedSchema = parser.parse(itemsSchema, scope.append("items"));
      return new ItemsValidator(parsedSchema);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for items keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null items keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("items");
  }

  class ItemsValidator implements AsyncValidator {

    private Schema schema;

    public ItemsValidator(Schema schema) {
      this.schema = schema;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future validate(Object in) {
      if (in instanceof JsonArray) {
        JsonArray arr = (JsonArray) in;
        List<Future> futs = new ArrayList<>();
        for (Object v : arr) {
          futs.add(schema.validate(v));
        }
        return CompositeFuture.all(futs).compose(cf -> Future.succeededFuture());
      } else return Future.succeededFuture();
    }
  }
}
