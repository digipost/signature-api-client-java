package no.digipost.signature.client.core;

import java.net.URI;

public class XAdESReference {

    public static XAdESReference of(URI url) {
        return url == null ? null : new XAdESReference(url);
    }

    private final URI xAdESUrl;

    private XAdESReference(URI xAdESUrl) {
        this.xAdESUrl = xAdESUrl;
    }

    public URI getxAdESUrl() {
        return xAdESUrl;
    }
}
