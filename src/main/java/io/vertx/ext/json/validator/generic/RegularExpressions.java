package io.vertx.ext.json.validator.generic;

import java.util.regex.Pattern;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RegularExpressions {

  public static final Pattern EMAIL = Pattern.compile("^(?:[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+\\.)" +
      "*[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)"
      + "+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}" +
      "(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$");
  public static final Pattern URI = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$");
  public static final Pattern DATE = Pattern.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}$");
  public static final Pattern DATETIME = Pattern.compile("^([0-9]+)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])[Tt]([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)(\\.[0-9]+)?(([Zz])|([\\+|\\-]([01][0-9]|2[0-3]):[0-5][0-9]))$");
  //Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])[tT]([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d|60)(\\.\\d+)?([zZ]|(\\+|-)([01]\\d|2[0-3]):([0-5]\\d))$");
  public static final Pattern TIME = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$");
  public static final Pattern BASE64 = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");
  public static final Pattern IPV4 = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" + "" +
      "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
  public static final Pattern IPV6 = Pattern.compile("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}" + "" +
      "(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(" +
      "([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." + "" +
      "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|(" + "" +
      "(:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|" +
      "(" + "([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:(" + "" +
      "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:)" +
      "{2}" + "(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." + "" +
      "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|(" + "" +
      "(:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))"
      + "|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." + "" +
      "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$");
  public static final Pattern HOSTNAME = Pattern.compile("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])\\.)*" + "" +
      "([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]{0,61}[A-Za-z0-9])$");
  public static final Pattern RELATIVE_JSON_POINTER = Pattern.compile("^[0-9]+(#|(/(([^/~])|(~[01]))*))*$");

}
