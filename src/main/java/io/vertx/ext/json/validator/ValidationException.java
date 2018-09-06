package io.vertx.ext.json.validator;

import io.vertx.core.VertxException;

/**
 * This is the main class for every Validation flow related errors
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ValidationException extends VertxException {

  /**
   * All errors type. You can get this values using {@link io.vertx.ext.web.api.validation.ValidationException#type()}
   */
  public enum ErrorType {
    /**
     * The provided priority not match with ParameterTypeValidator rules
     */
    NO_MATCH,
    /**
     * Parameter not found in request
     */
    NOT_FOUND,
    /**
     * It was expected a single priority, but found in request an array
     */
    UNEXPECTED_ARRAY,
    /**
     * It was expected an array, but found in request a single priority
     */
    UNEXPECTED_SINGLE_STRING,
    /**
     * Expected file not found
     */
    FILE_NOT_FOUND,
    /**
     * Wrong Content-Type header
     */
    WRONG_CONTENT_TYPE,
    /**
     * Parameter found but with empty priority
     */
    EMPTY_VALUE,
    /**
     * Expected an array size between parameters configured in
     * {@link io.vertx.ext.web.api.validation.impl.ArrayTypeValidator}
     */
    UNEXPECTED_ARRAY_SIZE,
    /**
     * Error during deserializaton with rule provided
     */
    DESERIALIZATION_ERROR,
    /**
     * Object field declared as required in {@link io.vertx.ext.web.api.validation.impl.ObjectTypeValidator} not found
     */
    OBJECT_FIELD_NOT_FOUND,
    /**
     * Json can't be parsed
     */
    JSON_NOT_PARSABLE,
    /**
     * Json doesn't match the provided schema
     */
    JSON_INVALID,
    /**
     * XML doesn't match the provided schema
     */
    XML_INVALID
  }

  private String parameterName;
  private String value;
  final private ErrorType errorType;

  private ValidationException(String message, String parameterName, String value, ErrorType errorType) {
    super((message != null && message.length() != 0) ? message : "ValidationException{" + "parameterName='" +
        parameterName + '\'' + ", priority='" + value + '\'' + ", errorType=" + errorType + '}');
    this.parameterName = parameterName;
    this.value = value;
    this.errorType = errorType;
  }

  public ValidationException(String message, ErrorType error) {
    this(message, null, null, error);
  }

  public ValidationException(ErrorType error) {
    this(null, null, null, error);
  }

  public ValidationException(String message) {
    this(message, null, null, null);
  }

  public String parameterName() {
    return parameterName;
  }

  public String value() {
    return value;
  }

  public ErrorType type() {
    return errorType;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "ValidationException{" + "parameterName='" + parameterName + '\'' + ", priority='" + value + '\'' + ", " +
        "errorType=" + errorType + ", message='" + getMessage() + "'}";
  }


}
