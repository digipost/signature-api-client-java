package no.digipost.signature.client.core;

import java.net.URI;

public class PAdESReference {

    public static PAdESReference of(String url) {
        return url == null ? null : new PAdESReference(URI.create(url));
    }

    private final URI pAdESUrl;

    private PAdESReference(URI url) {
        this.pAdESUrl = url;
    }

    public URI getpAdESUrl() {
        return pAdESUrl;
    }
}
