package no.digipost.signature.client.core.internal.http;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SignatureHttpClientFactoryTest implements HttpIntegrationConfiguration {

    @Test
    void instantiatesSignatureHttpClient() {
        SignatureHttpClient signatureHttpClient = SignatureHttpClientFactory.create(this);
        URI root = signatureHttpClient.signatureServiceRoot();
        assertThat(root, is(this.getServiceRoot()));
    }

    @Override
    public HttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }

    @Override
    public URI getServiceRoot() {
        return URI.create("localhost");
    }

}
