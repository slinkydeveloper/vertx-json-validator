package io.vertx.ext.json.validator.openapi3;

import io.vertx.ext.json.validator.FormatPredicate;
import io.vertx.ext.json.validator.generic.BaseFormatValidatorFactory;
import io.vertx.ext.json.validator.generic.RegularExpressions;

import java.util.HashMap;
import java.util.Map;

public class FormatValidatorFactory extends BaseFormatValidatorFactory {
  @Override
  public Map<String, FormatPredicate> initFormatsMap() {
    Map<String, FormatPredicate> predicates = new HashMap<>();
    predicates.put("byte", createPredicateFromPattern(RegularExpressions.BASE64));
    predicates.put("date", createPredicateFromPattern(RegularExpressions.DATE));
    predicates.put("date-time", createPredicateFromPattern(RegularExpressions.DATETIME));
    predicates.put("ipv4", createPredicateFromPattern(RegularExpressions.IPV4));
    predicates.put("ipv6", createPredicateFromPattern(RegularExpressions.IPV6));
    predicates.put("hostname", createPredicateFromPattern(RegularExpressions.HOSTNAME));
    predicates.put("email", createPredicateFromPattern(RegularExpressions.EMAIL));
    predicates.put("uri", URI_VALIDATOR);
    predicates.put("uriref", URI_REFERENCE_VALIDATOR);
    return predicates;
  }
}
