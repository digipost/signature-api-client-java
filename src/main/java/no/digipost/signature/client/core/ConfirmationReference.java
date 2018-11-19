package no.digipost.signature.client.core;

public class ConfirmationReference {

    public static ConfirmationReference of(String url) {
        return url == null ? null : new ConfirmationReference(url);
    }

    private final String url;

    private ConfirmationReference(String url) {
        this.url = url;
    }

    public String getConfirmationUrl() {
        return url;
    }
}
