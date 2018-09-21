package io.vertx.ext.json.validator.draft7;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vertx.ext.json.validator.ValidationErrorType.NO_MATCH;

public class DependenciesValidatorFactory implements ValidatorFactory {
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      JsonObject dependencies = schema.getJsonObject("dependencies");
      JsonPointer baseScope = scope.copy().append("dependencies");
      Map<String, Set<String>> keyDeps = new HashMap<>();
      Map<String, Schema> keySchemaDeps = new HashMap<>();
      for (Map.Entry<String, Object> entry : dependencies.getMap().entrySet()) {
        if (entry.getValue() instanceof Map || entry.getValue() instanceof Boolean) {
          keySchemaDeps.put(entry.getKey(), parser.parse((entry.getValue() instanceof Map) ? new JsonObject((Map<String, Object>) entry.getValue()) : entry.getValue(), baseScope.copy().append(entry.getKey())));
        } else {
          if (!((List)entry.getValue()).isEmpty())
            keyDeps.put(entry.getKey(), ((List<String>)entry.getValue()).stream().collect(Collectors.toSet()));
        }
      }
      if (keySchemaDeps.isEmpty()) return new DependenciesSyncValidator(keyDeps);
      else return new DependenciesAsyncValidator(keySchemaDeps, new DependenciesSyncValidator(keyDeps));
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for dependencies keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null dependencies keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("dependencies");
  }

  class DependenciesSyncValidator implements SyncValidator {

    final Map<String, Set<String>> keyDeps;

    public DependenciesSyncValidator(Map<String, Set<String>> keyDeps) {
      this.keyDeps = keyDeps;
    }

    @Override
    public void validate(Object value) throws ValidationException {
      if (value instanceof JsonObject) {
        JsonObject obj = (JsonObject) value;
        Set<String> objKeys = obj.getMap().keySet();
        for (Map.Entry<String, Set<String>> dependency : keyDeps.entrySet()) {
          if (obj.containsKey(dependency.getKey()) && !objKeys.containsAll(dependency.getValue()))
            throw NO_MATCH.createException("dependencies of key " + dependency.getKey() + " are not satisfied: " + dependency.getValue().toString(), "dependencies", value);
        }
      }
    }
  }

  class DependenciesAsyncValidator implements AsyncValidator {

    final Map<String, Schema> keySchemaDeps;
    final DependenciesSyncValidator syncValidator;

    public DependenciesAsyncValidator(Map<String, Schema> keySchemaDeps, DependenciesSyncValidator syncValidator) {
      this.keySchemaDeps = keySchemaDeps;
      this.syncValidator = syncValidator;
    }

    @Override
    public Future validate(Object in) {
      if (in instanceof JsonObject) {
        JsonObject obj = (JsonObject) in;
        try {
          syncValidator.validate(in);
          List<Future> futs = keySchemaDeps.entrySet().stream().filter(e -> obj.containsKey(e.getKey())).map(e -> e.getValue().validate(in)).collect(Collectors.toList());
          if (futs.isEmpty()) return Future.succeededFuture();
          else return CompositeFuture.all(futs).compose(cf -> Future.succeededFuture());
        } catch (ValidationException e) {
          return Future.failedFuture(e);
        }
      } else return Future.succeededFuture();
    }
  }

}
