package no.digipost.signature.client.portal;

import java.net.URI;

public class CancellationUrl {

    public static CancellationUrl of(URI url) {
        return url == null ? null : new CancellationUrl(url);
    }

    private final URI url;

    private CancellationUrl(URI url) {
        this.url = url;
    }

    public URI getUrl() {
        return url;
    }
}
