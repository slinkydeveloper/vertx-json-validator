package io.vertx.ext.json.validator.generic;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtils {

    public static URI replaceFragment(URI oldURI, String fragment) {
        try {
            return new URI(oldURI.getScheme(), oldURI.getSchemeSpecificPart(), fragment);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}
