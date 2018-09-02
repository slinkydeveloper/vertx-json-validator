package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.DataObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@DataObject
public class SchemaParserOptions {

    private List<ValidatorFactory> additionalValidatorFactories; //TODO put
    private Map<String, Predicate<String>> additionalStringFormatValidators;
    private boolean applyDefault;

    public SchemaParserOptions() {
        this.additionalValidatorFactories = new ArrayList<>();
        this.additionalStringFormatValidators = new HashMap<>();
        this.applyDefault = false;
    }

    public List<ValidatorFactory> getAdditionalValidatorFactories() {
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
