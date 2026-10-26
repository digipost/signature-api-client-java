package no.digipost.signature.client.core.internal.oauth;

import no.digipost.signature.client.TestClientCertificateRecordingServer;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.core.internal.configuration.Configurer;
import no.digipost.signature.client.security.oauth.ClientId;
import no.digipost.signature.client.security.oauth.OAuthConfiguration;
import no.digipost.signature.client.security.oauth.Resource;
import no.digipost.signature.client.security.oauth.Scope;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static no.digipost.signature.client.TestServerCertificate.LOCALHOST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;

/**
 * Verifies that the client certificate is presented to the token endpoint. The client uses the
 * JVM's default trust store, so it is pointed at the test server's {@code localhost} certificate.
 */
class MutualTlsTokenProviderClientCertificateTest {

    private static final String TOKEN_RESPONSE = "{\"access_token\":\"a-token\",\"expires_in\":3600}";


    @Test
    void presentsTheClientCertificateToTheTokenEndpoint() {
        try (TestClientCertificateRecordingServer tokenEndpoint = startTokenEndpoint()) {

            MutualTlsTokenProvider tokenProvider = LOCALHOST.trustedByDefaultWhile(
                    () -> MutualTlsTokenProvider.create(
                            oAuthConfiguration(tokenEndpoint.baseUri()), CLIENT_KEYSTORE,
                            Configurer.notConfigured(), Clock.systemUTC()));

            assertThat(tokenProvider.getToken(), is("a-token"));

            List<Optional<X509Certificate>> presented = tokenEndpoint.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), contains(CLIENT_KEYSTORE.getCertificate()));
        }
    }

    /**
     * The token endpoint is not covered by the API's trust configuration.
     */
    @Test
    void refusesATokenEndpointWhoseCertificateTheJvmDoesNotTrust() {
        try (TestClientCertificateRecordingServer tokenEndpoint = startTokenEndpoint()) {

            // No trust store override, so the test server's certificate is unknown
            MutualTlsTokenProvider tokenProvider = MutualTlsTokenProvider.create(
                    oAuthConfiguration(tokenEndpoint.baseUri()), CLIENT_KEYSTORE,
                    Configurer.notConfigured(), Clock.systemUTC());

            assertThrows(HttpIOException.class, tokenProvider::getToken);
            assertThat(tokenEndpoint.presentedClientCertificates(), hasSize(0));
        }
    }


    private static TestClientCertificateRecordingServer startTokenEndpoint() {
        return TestClientCertificateRecordingServer.start(LOCALHOST.serverSslContext(), "application/json", TOKEN_RESPONSE);
    }

    private static OAuthConfiguration oAuthConfiguration(URI tokenEndpointBaseUri) {
        return new OAuthConfiguration(
                URI.create(tokenEndpointBaseUri + "/token"),
                new Resource("https://api.example.com"),
                new ClientId("my-client-id"),
                Scope.fromAccountId(555444));
    }

}
