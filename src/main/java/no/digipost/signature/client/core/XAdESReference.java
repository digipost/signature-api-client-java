package no.digipost.signature.client.core;

public class XAdESReference {

    public static XAdESReference of(String url) {
        return url == null ? null : new XAdESReference(url);
    }

    private final String xAdESUrl;

    private XAdESReference(String xAdESUrl) {
        this.xAdESUrl = xAdESUrl;
    }

    public String getxAdESUrl() {
        return xAdESUrl;
    }
}
