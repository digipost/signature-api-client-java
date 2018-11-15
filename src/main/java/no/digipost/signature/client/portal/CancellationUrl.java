package no.digipost.signature.client.portal;

public class CancellationUrl {

    public static CancellationUrl of(String url) {
        return url == null ? null : new CancellationUrl(url);
    }

    private final String url;

    private CancellationUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
