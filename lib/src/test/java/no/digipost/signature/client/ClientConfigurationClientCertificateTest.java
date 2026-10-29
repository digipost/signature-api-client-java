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
import javax.net.ssl.SSLException;

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
import static no.digipost.signature.client.TestServerCertificate.LOCALHOST;
import static no.digipost.signature.client.TestServerCertificate.OTHER_HOSTNAME;
import static no.digipost.signature.client.security.CertificateChainValidation.Result.TRUSTED_AND_SKIP_FURTHER_VALIDATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;
import static uk.co.probablyfine.matchers.OptionalMatchers.empty;

/**
 * Verifies with real TLS handshakes which client certificate is presented to the API, and how the
 * API's certificate is validated.
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
        try (TestClientCertificateRecordingServer apiServer = startApiServer(organizationCertificateServerSslContext())) {

            callApi(configFor(apiServer.baseUri()).build());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), contains(CLIENT_KEYSTORE.getCertificate()));
        }
    }

    @Test
    void doesNotPresentTheClientCertificateWhenUsingOAuth() {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer(LOCALHOST.serverSslContext())) {

            callApi(LOCALHOST.trustedByDefaultWhile(configFor(apiServer.baseUri()).useOAuth(CLIENT_ID, SCOPE)::build));

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), empty());
        }
    }

    @Test
    void doesNotPresentTheClientCertificateForDocumentDownloadsEither() {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer(LOCALHOST.serverSslContext())) {
            ClientConfiguration config = LOCALHOST.trustedByDefaultWhile(configFor(apiServer.baseUri()).useOAuth(CLIENT_ID, SCOPE)::build);

            call(config.httpClientForDocumentDownloads(), apiServer.baseUri());

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), empty());
        }
    }

    /**
     * The trust configuration for the organization certificate, here overridden to trust anything, does not apply with OAuth.
     */
    @Test
    void refusesAnApiCertificateWhichTheJvmDoesNotTrustWhenUsingOAuth() {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer(LOCALHOST.serverSslContext())) {
            ClientConfiguration config = configFor(apiServer.baseUri()).useOAuth(CLIENT_ID, SCOPE).build();

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> callApi(config));
            assertThat(thrown, where(Throwable::getCause, instanceOf(SSLException.class)));
            assertThat(apiServer.presentedClientCertificates(), hasSize(0));
        }
    }

    @Test
    void refusesAnApiCertificateIssuedForAnotherHostnameWhenUsingOAuth() {
        stubTokenEndpoint();

        try (TestClientCertificateRecordingServer apiServer = startApiServer(OTHER_HOSTNAME.serverSslContext())) {
            ClientConfiguration config = OTHER_HOSTNAME.trustedByDefaultWhile(configFor(apiServer.baseUri()).useOAuth(CLIENT_ID, SCOPE)::build);

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> callApi(config));
            assertThat(thrown, where(Throwable::getCause, instanceOf(SSLException.class)));
            assertThat(apiServer.presentedClientCertificates(), hasSize(0));
        }
    }

    @Test
    void usingOAuthDoesNotAffectConfigurationsBuiltBeforeByTheSameBuilder() {
        try (TestClientCertificateRecordingServer apiServer = startApiServer(organizationCertificateServerSslContext())) {
            ClientConfiguration.Builder builder = configFor(apiServer.baseUri());
            ClientConfiguration withOrganizationCertificate = builder.build();
            builder.useOAuth(CLIENT_ID, SCOPE).build();

            callApi(withOrganizationCertificate);

            List<Optional<X509Certificate>> presented = apiServer.presentedClientCertificates();
            assertThat(presented, hasSize(1));
            assertThat(presented.get(0), contains(CLIENT_KEYSTORE.getCertificate()));
        }
    }


    /**
     * Presents the same certificate as the client, which is expired and not issued for a hostname,
     * and hence only trusted when overriding the validation of the organization certificate.
     */
    private static SSLContext organizationCertificateServerSslContext() {
        return TestClientCertificateRecordingServer.sslContextPresenting(
                CLIENT_KEYSTORE.keyStore, CLIENT_KEYSTORE.privatekeyPassword.toCharArray());
    }

    private static TestClientCertificateRecordingServer startApiServer(SSLContext serverSslContext) {
        return TestClientCertificateRecordingServer.start(serverSslContext, "text/plain", "ok");
    }

    private static void stubTokenEndpoint() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));
    }

    private ClientConfiguration.Builder configFor(URI apiBaseUri) {
        return ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(STAGING
                        .withServiceUrl(apiBaseUri)
                        .withOAuth(apiBaseUri, tokenEndpoint, STAGING.oAuth().resource()))
                .defaultSender(new Sender("123456789"))
                .serverCertificateTrustStrategy(certificateChain -> TRUSTED_AND_SKIP_FURTHER_VALIDATION);
    }

    private static void callApi(ClientConfiguration config) {
        call(config.defaultHttpClient(), config.signatureServiceRootUrl());
    }

    private static void call(HttpClient httpClient, URI apiBaseUri) {
        ClassicHttpRequest request = ClassicRequestBuilder.get(apiBaseUri + "/any-resource").build();
        runUnchecked(() -> httpClient.execute(request, response -> {
            EntityUtils.consume(response.getEntity());
            return null;
        }));
    }

}
