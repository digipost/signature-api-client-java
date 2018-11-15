package no.digipost.signature.client.direct;

public interface WithExitUrls {

    String getCompletionUrl();

    String getRejectionUrl();

    String getErrorUrl();

}
