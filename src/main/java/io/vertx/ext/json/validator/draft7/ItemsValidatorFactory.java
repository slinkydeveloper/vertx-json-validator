package io.vertx.ext.json.validator.draft7;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.ArrayList;
import java.util.List;

public class ItemsValidatorFactory extends io.vertx.ext.json.validator.generic.ItemsValidatorFactory {

  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    Object itemsSchema = schema.getValue("items");
    if (itemsSchema instanceof JsonArray) {
      try {
        JsonPointer baseScope = scope.copy().append("items");
        JsonArray itemsList = (JsonArray) itemsSchema;
        List<Schema> parsedSchemas = new ArrayList<>();
        for (int i = 0; i < itemsList.size(); i++) {
          parsedSchemas.add(i, parser.parse(itemsList.getValue(i), baseScope.copy().append(Integer.toString(i))));
        }
        if (schema.containsKey("additionalItems"))
          return new ItemByItemValidator(parsedSchemas.toArray(new Schema[parsedSchemas.size()]), parser.parse(schema.getValue("additionalItems"), scope.copy().append("additionalItems")));
        else
          return new ItemByItemValidator(parsedSchemas.toArray(new Schema[parsedSchemas.size()]), null);
      } catch (NullPointerException e) {
        throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null items keyword");
      }
    } else {
      return super.createValidator(schema, scope, parser);
    }
  }

  class ItemByItemValidator implements AsyncValidator {

    final Schema[] schemas;
    final Schema additionalItems;

    public ItemByItemValidator(Schema[] schemas, Schema additionalItems) {
      this.schemas = schemas;
      this.additionalItems = additionalItems;
    }

    @Override
    public Future<Void> validate(Object in) {
      if (in instanceof JsonArray) {
        List<Future> futures = new ArrayList<>();
        JsonArray arr = (JsonArray) in;
        for (int i = 0; i < arr.size(); i++) {
          Future<Void> fut;
          if (i >= schemas.length) {
            if (additionalItems != null)
              fut = additionalItems.validate(arr.getValue(i));
            else continue;
          } else fut = schemas[i].validate(arr.getValue(i));
          if (fut.isComplete()) {
            if (fut.failed()) return Future.failedFuture(fut.cause());
          } else {
            futures.add(fut);
          }
        }
        if (futures.isEmpty()) return Future.succeededFuture();
        else return CompositeFuture.all(futures).compose(cf -> Future.succeededFuture());
      } else return Future.succeededFuture();
    }
  }

}
