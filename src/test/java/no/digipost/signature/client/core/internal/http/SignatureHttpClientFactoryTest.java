package no.digipost.signature.client.core.internal.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

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
        return HttpClient.newHttpClient();
    }

    @Override
    public URI getServiceRoot() {
        return URI.create("localhost");
    }

    @Override
    public Duration socketTimeout() {
        return Duration.ofSeconds(10);
    }

}
