package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.DataObject;

import java.util.Map;
import java.util.function.Predicate;

@DataObject
public class SchemaParserOptions {

    private Map<String, ValidatorFactory> additionalValidatorFactories; //TODO put
    private Map<String, Predicate<String>> additionalStringFormatValidators;
    private boolean applyDefault;
    //TODO scope

    public Map<String, ValidatorFactory> getAdditionalValidatorFactories() {
        return additionalValidatorFactories;
    }

    public Map<String, Predicate<String>> getAdditionalStringFormatValidators() {
        return additionalStringFormatValidators;
    }

    public boolean isApplyDefault() {
        return applyDefault;
    }

    public void setApplyDefault(boolean applyDefault) {
        this.applyDefault = applyDefault;
    }
}
