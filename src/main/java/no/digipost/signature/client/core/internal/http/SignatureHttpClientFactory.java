package no.digipost.signature.client.core.internal.http;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import java.net.URI;

public class SignatureHttpClientFactory {


    public static SignatureHttpClient create(HttpIntegrationConfiguration config) {
        JerseyClientBuilder jerseyBuilder = (JerseyClientBuilder) JerseyClientBuilder.newBuilder();
        JerseyClient jerseyClient = jerseyBuilder
                .withConfig(config.getJaxrsConfiguration())
                .sslContext(config.getSSLContext())
                .hostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        try {
            jerseyClient.preInitialize();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to pre-initialize Jersey Client, because " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'. " +
                    "This step is taken to ensure everything the client needs is available already on instantiation, " +
                    "in particular the InjectionManager facilities used internally by Jersey. By default, the Signature API Client " +
                    "should include the Jersey HK2 implementation, but if you need to control this yourself, consider excluding " +
                    "org.glassfish.jersey.inject:jersey-hk2 when depending on the signature-api-client-java, and make sure to make the " +
                    "InjectionManagerFactory of your choice discoverable by Jersey.", e);
        }
        return new DefaultClient(jerseyClient, config.getServiceRoot());
    }



    private static final class DefaultClient implements SignatureHttpClient {

        private final Client jerseyClient;
        private final WebTarget signatureServiceRoot;

        DefaultClient(Client jerseyClient, URI root) {
            this.jerseyClient = jerseyClient;
            this.signatureServiceRoot = jerseyClient.target(root);
        }

        @Override
        public WebTarget target(URI uri) {
            return jerseyClient.target(uri);
        }

        @Override
        public WebTarget signatureServiceRoot() {
            return signatureServiceRoot;
        }

    }

}
