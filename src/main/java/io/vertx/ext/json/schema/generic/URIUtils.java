package io.vertx.ext.json.schema.generic;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtils {

  public static URI removeFragment(URI oldURI) {
    return URIUtils.replaceFragment(oldURI, null);
  }

  public static URI replaceFragment(URI oldURI, String fragment) {
    try {
      if (oldURI != null) {
        return new URI(oldURI.getScheme(), oldURI.getSchemeSpecificPart(), fragment);
      } else return new URI(null, null, fragment);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean isRemoteURI(URI uri) {
    return "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
  }

  public static URI resolvePath(URI oldURI, String path) {
    try {
      if ("jar".equals(oldURI.getScheme())) {
        String[] splittedJarURI = oldURI.getSchemeSpecificPart().split("!");
        String newInternalJarPath = URI.create(splittedJarURI[1]).resolve(path).toString();
        return new URI(oldURI.getScheme(), splittedJarURI[0] + "!" + newInternalJarPath, oldURI.getFragment());
      } else
        return oldURI.resolve(path);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

}
