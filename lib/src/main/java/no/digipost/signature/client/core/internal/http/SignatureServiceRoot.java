package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.core.WithSignatureServiceRootUrl;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.UnaryOperator;

public final class SignatureServiceRoot {

    public static SignatureServiceRoot from(WithSignatureServiceRootUrl configuration) {
        return new SignatureServiceRoot(configuration.signatureServiceRootUrl());
    }


    private final URI rootUrl;

    public SignatureServiceRoot(URI rootUrl) {
        if (!rootUrl.isAbsolute()) {
            throw new IllegalArgumentException(rootUrl + " must be an absolute URL");
        }
        this.rootUrl = rootUrl;
    }

    public URI rootUrl() {
        return rootUrl;
    }

    public URI constructUrl(UnaryOperator<URIBuilder> uri) {
        URI serviceRoot = rootUrl();
        URIBuilder uriBuilder = uri.apply(new URIBuilder(serviceRoot));
        try {
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Invalid URL constructed for service at " + serviceRoot + ": " + uriBuilder + ". " +
                    "Reason: " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
        }
    }

    @Override
    public String toString() {
        return "SignatureServiceRoot '" + rootUrl + "'";
    }


}
