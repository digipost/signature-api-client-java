package no.digipost.signature.client.core;

public class DeleteDocumentsUrl {

    public static DeleteDocumentsUrl of(String url) {
        return url == null ? null : new DeleteDocumentsUrl(url);
    }

    private final String url;

    private DeleteDocumentsUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
