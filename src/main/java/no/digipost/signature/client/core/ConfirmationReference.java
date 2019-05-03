package no.digipost.signature.client.core;

import java.net.URI;

public class ConfirmationReference {

    public static ConfirmationReference of(String url) {
        return url == null ? null : new ConfirmationReference(URI.create(url));
    }

    private final URI url;

    private ConfirmationReference(URI url) {
        this.url = url;
    }

    public URI getConfirmationUrl() {
        return url;
    }
}
