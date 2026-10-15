package no.digipost.signature.client;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.security.oauth.ClientId;
import no.digipost.signature.client.security.oauth.Scope;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.digipost.DiggExceptions.runUnchecked;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static no.digipost.signature.client.security.CertificateChainValidation.Result.TRUSTED_AND_SKIP_FURTHER_VALIDATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;
import static uk.co.probablyfine.matchers.OptionalMatchers.empty;

/**
 * Verifies with a real TLS handshake that no client certificate is presented to the API.
 */
@WireMockTest
class ClientConfigurationClientCertificateTest {

    private static final String TOKEN_PATH = "/token";
    private static final ClientId CLIENT_ID = new ClientId("my-client-id");
    private static final Scope SCOPE = Scope.fromAccountId(555444);

    private final URI tokenEndpoint;

    ClientConfigurationClientCertificateTest(WireMockRuntimeInfo wireMockInfo) {
        this.tokenEndpoint = URI.create(wireMockInfo.getHttpBaseUrl() + TOKEN_PATH);
    }


    @Test
    void presentsTheClientCertificateWhenAuthenticatingWithTheCertificate() {
        try (TestClientCertificateRecordingServer apiServer = startApiServer()) {

            callApi(configFor(apiServer.baseUri()).build(), apiServer.baseUri());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), contains(CLIENT_KEYSTORE.getCertificate()));
        }
    }

    @Test
    void doesNotPresentTheClientCertificateWhenAuthenticatingWithAnAccessToken() {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer()) {

            callApi(configFor(apiServer.baseUri()).useOAuth(CLIENT_ID, SCOPE).build(), apiServer.baseUri());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), empty());
        }
    }

    @Test
    void doesNotPresentTheClientCertificateForDocumentDownloadsEither() {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer()) {
            ClientConfiguration config = configFor(apiServer.baseUri()).useOAuth(CLIENT_ID, SCOPE).build();

            call(config.httpClientForDocumentDownloads(), apiServer.baseUri());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), empty());
        }
    }


    private static TestClientCertificateRecordingServer startApiServer() {
        SSLContext serverSslContext = TestClientCertificateRecordingServer.sslContextPresenting(
                CLIENT_KEYSTORE.keyStore, CLIENT_KEYSTORE.privatekeyPassword.toCharArray());
        return TestClientCertificateRecordingServer.start(serverSslContext, "text/plain", "ok");
    }

    private static void stubTokenEndpoint() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));
    }

    private ClientConfiguration.Builder configFor(URI apiBaseUri) {
        return ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(STAGING.withServiceUrl(apiBaseUri).withTokenEndpoint(tokenEndpoint))
                .defaultSender(new Sender("123456789"))
                .serverCertificateTrustStrategy(certificateChain -> TRUSTED_AND_SKIP_FURTHER_VALIDATION);
    }

    private static void callApi(ClientConfiguration config, URI apiBaseUri) {
        call(config.defaultHttpClient(), apiBaseUri);
    }

    private static void call(HttpClient httpClient, URI apiBaseUri) {
        ClassicHttpRequest request = ClassicRequestBuilder.get(apiBaseUri + "/any-resource").build();
        runUnchecked(() -> httpClient.execute(request, response -> {
            EntityUtils.consume(response.getEntity());
            return null;
        }));
    }

}
