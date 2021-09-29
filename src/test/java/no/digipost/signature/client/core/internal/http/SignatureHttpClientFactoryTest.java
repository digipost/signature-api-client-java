package no.digipost.signature.client.core.internal.http;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;

import java.net.URI;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

public class SignatureHttpClientFactoryTest implements HttpIntegrationConfiguration {

    @Test
    void instantiatesSignatureHttpClient() {
        SignatureHttpClient signatureHttpClient = SignatureHttpClientFactory.create(this);
        WebTarget target = signatureHttpClient.signatureServiceRoot();
        assertThat(target, where(WebTarget::getUri, is(this.getServiceRoot())));
    }




    @Override
    public Configuration getJaxrsConfiguration() {
        return new ClientConfig();
    }

    @Override
    public SSLContext getSSLContext() {
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public URI getServiceRoot() {
        return URI.create("localhost");
    }

}
