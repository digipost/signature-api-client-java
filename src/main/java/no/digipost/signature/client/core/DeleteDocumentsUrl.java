package no.digipost.signature.client.core;

import java.net.URI;

public class DeleteDocumentsUrl {

    public static DeleteDocumentsUrl of(String url) {
        return url == null ? null : new DeleteDocumentsUrl(URI.create(url));
    }

    private final URI url;

    private DeleteDocumentsUrl(URI url) {
        this.url = url;
    }

    public URI getUrl() {
        return url;
    }
}
