package io.vertx.ext.json.validator;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@DataObject
public class SchemaParserOptions {

  private List<ValidatorFactory> additionalValidatorFactories;
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

  /**
   * Add a validator factory that will be applied to {@link SchemaParser}
   *
   * @return
   */
  @Fluent
  public SchemaParserOptions putAdditionalValidatorFactory(ValidatorFactory factory) {
    this.additionalValidatorFactories.add(factory);
    return this;
  }

  /**
   * Add a format validator that will be applied to {@link SchemaParser}
   *
   * @return
   */
  @Fluent
  public SchemaParserOptions putAdditionalStringFormatValidator(String formatName, Predicate<String> predicate) {
    this.additionalStringFormatValidators.put(formatName, predicate);
    return this;
  }

  public boolean isApplyDefault() {
    return applyDefault;
  }

  public void setApplyDefault(boolean applyDefault) {
    this.applyDefault = applyDefault;
  }
}
