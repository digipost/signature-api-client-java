package no.digipost.signature.client;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.security.CertificateChainValidation;
import no.digipost.signature.client.security.BrokerId;
import no.digipost.signature.client.security.JwtAuthConfig;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;
import static uk.co.probablyfine.matchers.OptionalMatchers.empty;

/**
 * Verifies which certificate, if any, is actually presented to the API. This uses a real TLS
 * handshake against a local server, as no amount of inspecting the client configuration can prove
 * what ends up on the wire.
 *
 * @see no.digipost.signature.client.core.internal.http.MutualTlsTokenProviderClientCertificateTest
 * for the same kind of assertion on the connection to the token endpoint
 */
@WireMockTest
class ClientConfigurationClientCertificateTest {

    private static final String TOKEN_PATH = "/token";

    private final JwtAuthConfig jwtAuthConfig;

    private final URI tokenEndpoint;

    ClientConfigurationClientCertificateTest(WireMockRuntimeInfo wireMockInfo) {
        this.jwtAuthConfig = JwtAuthConfig.forClient("my-client-id", BrokerId.of("555444"));
        this.tokenEndpoint = URI.create(wireMockInfo.getHttpBaseUrl() + TOKEN_PATH);
    }


    @Test
    void presentsTheClientCertificateWhenAuthenticatingWithTheCertificate() throws Exception {
        try (TestClientCertificateRecordingServer apiServer = startApiServer()) {

            callApi(configFor(apiServer.baseUri()).build(), apiServer.baseUri());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), contains(CLIENT_KEYSTORE.getCertificate()));
        }
    }

    @Test
    void doesNotPresentTheClientCertificateWhenAuthenticatingWithAnAccessToken() throws Exception {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer()) {

            callApi(configFor(apiServer.baseUri()).jwtAuthentication(jwtAuthConfig).build(), apiServer.baseUri());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), empty());
        }
    }

    @Test
    void doesNotPresentTheClientCertificateForDocumentDownloadsEither() throws Exception {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer()) {
            ClientConfiguration config = configFor(apiServer.baseUri()).jwtAuthentication(jwtAuthConfig).build();

            call(config.httpClientForDocumentDownloads(), apiServer.baseUri());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), empty());
        }
    }


    /**
     * The API test server presents the test client certificate. Which certificate it is does not
     * matter here, as the client is configured below to accept it as-is.
     */
    private static TestClientCertificateRecordingServer startApiServer() throws IOException, GeneralSecurityException {
        SSLContext serverSslContext = TestClientCertificateRecordingServer.sslContextPresenting(
                CLIENT_KEYSTORE.keyStore, CLIENT_KEYSTORE.privatekeyPassword.toCharArray());
        return TestClientCertificateRecordingServer.start(serverSslContext, "text/plain", "ok");
    }

    private static void stubTokenEndpoint() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));
    }

    /**
     * The test server presents a self-signed certificate, so the usual validation that the server
     * identifies itself as Posten Bring AS is replaced with one which accepts it as-is.
     */
    private ClientConfiguration.Builder configFor(URI apiBaseUri) {
        return ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(STAGING.withServiceUrl(apiBaseUri).withTokenEndpoint(tokenEndpoint))
                .defaultSender(new Sender("123456789"))
                .serverCertificateTrustStrategy(
                        certificateChain -> CertificateChainValidation.Result.TRUSTED_AND_SKIP_FURTHER_VALIDATION);
    }

    private static void callApi(ClientConfiguration config, URI apiBaseUri) throws IOException {
        call(config.defaultHttpClient(), apiBaseUri);
    }

    private static void call(HttpClient httpClient, URI apiBaseUri) throws IOException {
        ClassicHttpRequest request = ClassicRequestBuilder.get(apiBaseUri + "/any-resource").build();
        httpClient.execute(request, response -> {
            EntityUtils.consume(response.getEntity());
            return null;
        });
    }

}
