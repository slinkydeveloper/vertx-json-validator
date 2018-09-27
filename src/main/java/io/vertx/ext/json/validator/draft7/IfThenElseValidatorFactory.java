package io.vertx.ext.json.validator.draft7;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;
import io.vertx.ext.json.validator.generic.FutureUtils;

import java.util.Map;

public class IfThenElseValidatorFactory implements ValidatorFactory {
  @Override
  public Validator createValidator(JsonObject schema, JsonPointer scope, SchemaParser parser) {
    try {
      Object conditionSchemaUnparsed = schema.getValue("if");
      Schema conditionSchema = parser.parse((conditionSchemaUnparsed instanceof Map) ? new JsonObject((Map<String, Object>) conditionSchemaUnparsed) : conditionSchemaUnparsed, scope.copy().append("if"));
      Object thenSchemaUnparsed = schema.getValue("then");
      Schema thenSchema = (thenSchemaUnparsed == null) ? null : parser.parse((thenSchemaUnparsed instanceof Map) ? new JsonObject((Map<String, Object>) thenSchemaUnparsed) : thenSchemaUnparsed, scope.copy().append("if"));
      Object elseSchemaUnparsed = schema.getValue("else");
      Schema elseSchema = (elseSchemaUnparsed == null) ? null : parser.parse((elseSchemaUnparsed instanceof Map) ? new JsonObject((Map<String, Object>) elseSchemaUnparsed) : elseSchemaUnparsed, scope.copy().append("if"));
      return new IfThenElseValidator(conditionSchema, thenSchema, elseSchema);
    } catch (ClassCastException e) {
      throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "Wrong type for if/then/else keyword");
    } catch (NullPointerException e) {
      throw SchemaErrorType.NULL_KEYWORD_VALUE.createException(schema, "Null if/then/else keyword");
    }
  }

  @Override
  public boolean canConsumeSchema(JsonObject schema) {
    return schema.containsKey("if") && (schema.containsKey("then") || schema.containsKey("else"));
  }

  class IfThenElseValidator implements AsyncValidator {

    private final Schema condition;
    private final Schema thenBranch;
    private final Schema elseBranch;

    public IfThenElseValidator(Schema condition, Schema thenBranch, Schema elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public Future<Void> validate(Object in) {
      return FutureUtils.andThen(
          this.condition.validate(in),
          o -> (this.thenBranch != null) ? this.thenBranch.validate(in): Future.succeededFuture(),
          o -> (this.elseBranch != null) ? this.elseBranch.validate(in) : Future.succeededFuture()
      );
    }
  }

}
