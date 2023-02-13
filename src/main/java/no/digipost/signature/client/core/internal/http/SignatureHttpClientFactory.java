package no.digipost.signature.client.core.internal.http;

import org.apache.hc.client5.http.classic.HttpClient;

import java.net.URI;

public class SignatureHttpClientFactory {

    public static SignatureHttpClient create(HttpIntegrationConfiguration config) {
        return new DefaultClient(config.httpClient(), config.getServiceRoot());
    }



    private static final class DefaultClient implements SignatureHttpClient {

        private final HttpClient httpClient;
        private final URI signatureServiceRoot;

        DefaultClient(HttpClient httpClient, URI root) {
            this.httpClient = httpClient;
            this.signatureServiceRoot = root;
        }

        @Override
        public URI signatureServiceRoot() {
            return signatureServiceRoot;
        }

        public HttpClient httpClient() {
            return httpClient;
        }

    }

}
