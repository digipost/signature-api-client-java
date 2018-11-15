package no.digipost.signature.client.core;

public class PAdESReference {

    public static PAdESReference of(String url) {
        return url == null ? null : new PAdESReference(url);
    }

    private final String pAdESUrl;

    private PAdESReference(String pAdESUrl) {
        this.pAdESUrl = pAdESUrl;
    }

    public String getpAdESUrl() {
        return pAdESUrl;
    }
}
