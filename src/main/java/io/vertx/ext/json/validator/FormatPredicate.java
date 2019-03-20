package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.VertxGen;

@VertxGen
@FunctionalInterface
public interface FormatPredicate {

  boolean isValid(String s);

}
