package no.digipost.signature.client.core.internal.http;

import java.net.URI;
import java.net.URISyntaxException;

public class UriHelper {

    public static URI addQuery(URI uri, String query) {
        String newQuery = uri.getQuery();
        if (newQuery == null) {
            newQuery = query;
        } else {
            newQuery += "&" + query;
        }

        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, uri.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not append query [" + query + "] to uri [" + uri + "]", e);
        }
    }
}
