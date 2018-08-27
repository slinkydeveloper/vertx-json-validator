package io.vertx.ext.json.validator;

import io.vertx.core.Future;

@FunctionalInterface
public interface AsyncValidator extends Validator {
    @Override
    default boolean isAsync() {
        return true;
    }

    @Override
    default ValidatorPriority getPriority(){
        return ValidatorPriority.MIN_PRIORITY;
    }

    Future validate(Object in);
}
