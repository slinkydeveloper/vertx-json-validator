package io.vertx.ext.json.validator;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ValidationExceptionFactory {

  public static ValidationException generateUnexpectedArraySizeValidationException(Integer maxItems, Integer
          minItems, Integer actualSize) {
    return new ValidationException("Array parameter have unexpected size: " + minItems + "<=" + actualSize + "<=" +
            maxItems, ValidationException.ErrorType.UNEXPECTED_ARRAY_SIZE);
  }

  public static ValidationException generateDeserializationError(String message) {
    return new ValidationException(message, ValidationException.ErrorType.DESERIALIZATION_ERROR);
  }

  public static ValidationException generateObjectFieldNotFound(String fieldName) {
    return new ValidationException("Object field not found but required: " + fieldName, ValidationException.ErrorType.OBJECT_FIELD_NOT_FOUND);
  }

  public static ValidationException generateNotMatchValidationException(String message) {
    return new ValidationException(message, ValidationException.ErrorType.NO_MATCH);
  }

  public static ValidationException generateNotParsableJsonBodyException(String message) {
    return new ValidationException(message, ValidationException.ErrorType.JSON_NOT_PARSABLE);
  }

  public static ValidationException generateInvalidJsonBodyException(String message) {
    return new ValidationException(message, ValidationException.ErrorType.JSON_INVALID);
  }

  public static ValidationException generateInvalidXMLBodyException(String message) {
    return new ValidationException(message, ValidationException.ErrorType.XML_INVALID);
  }

}
