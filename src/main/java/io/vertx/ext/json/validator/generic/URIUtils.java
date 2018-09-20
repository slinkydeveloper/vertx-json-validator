package io.vertx.ext.json.validator.generic;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

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

  public static URI replaceOrResolvePath(URI oldURI, String path) {
    try {
      if ("file".equals(oldURI.getScheme()) || "jar".equals(oldURI.getScheme())) {
        return new URI(oldURI.getScheme(), oldURI.getHost(), Paths.get(oldURI.getPath()).getParent().resolve(Paths.get(path)).toString(), null);
      } else
        return oldURI.resolve(path);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

}
