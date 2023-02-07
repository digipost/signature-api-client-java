package no.digipost.signature.client.core.internal.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class SignatureHttpClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureHttpClientFactory.class);

    public static SignatureHttpClient create(HttpIntegrationConfiguration config) {
        return new DefaultClient(config.httpClient(), config.getServiceRoot(), config.socketTimeout());
    }



    private static final class DefaultClient implements SignatureHttpClient {

        private final HttpClient httpClient;
        private final URI signatureServiceRoot;
        private final Duration socketTimeout;

        DefaultClient(HttpClient httpClient, URI root, Duration socketTimeout) {
            this.httpClient = httpClient;
            this.signatureServiceRoot = root;
            this.socketTimeout = socketTimeout;
        }

        @Override
        public URI signatureServiceRoot() {
            return signatureServiceRoot;
        }

        public HttpClient httpClient() {
            return httpClient;
        }

        public Duration socketTimeout() {
            return socketTimeout;
        }
    }

}
