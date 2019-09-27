package no.digipost.signature.client.core;

import java.net.URI;

public class DeleteDocumentsUrl {

    public static DeleteDocumentsUrl of(URI url) {
        return url == null ? null : new DeleteDocumentsUrl(url);
    }

    private final URI url;

    private DeleteDocumentsUrl(URI url) {
        this.url = url;
    }

    public URI getUrl() {
        return url;
    }
}
