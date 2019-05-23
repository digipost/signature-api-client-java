package no.digipost.signature.client.direct;

import java.net.URI;

public interface WithExitUrls {

    URI getCompletionUrl();

    URI getRejectionUrl();

    URI getErrorUrl();

}
