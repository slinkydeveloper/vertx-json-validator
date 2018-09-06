package io.vertx.ext.json.validator.generic;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class URIUtils {

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

  public static URI replacePath(URI oldURI, String path) {
    try {
      if ("file".equals(oldURI.getScheme())) {
        return new URI(oldURI.getScheme(), oldURI.getHost(), Paths.get(oldURI.getPath()).getParent().resolve(Paths.get(path)).toString(), null);
      } else
        return new URI(oldURI.getScheme(), oldURI.getHost(), (path.charAt(0) == '/') ? path : "/" + path, null);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

}
