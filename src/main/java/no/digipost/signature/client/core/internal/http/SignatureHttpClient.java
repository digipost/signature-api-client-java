package no.digipost.signature.client.core.internal.http;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.UnaryOperator;

public interface SignatureHttpClient {

    URI signatureServiceRoot();

    HttpClient httpClient();

    default URI constructUrl(UnaryOperator<URIBuilder> uri) {
        URI serviceRoot = signatureServiceRoot();
        URIBuilder uriBuilder = uri.apply(new URIBuilder());
        try {
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "Invalid URL constructed for service at " + serviceRoot + ": " + uriBuilder + ". " +
                    "Reason: " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
        }
    }

}
