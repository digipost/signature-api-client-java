package no.digipost.signature.client;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.SignatureException;
import no.digipost.signature.client.direct.DirectClient;
import no.digipost.signature.client.direct.WithSignerUrl;
import no.digipost.signature.client.portal.PortalClient;
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;
import no.digipost.signature.client.security.BrokerId;
import no.digipost.signature.client.security.JwtAuthConfig;
import no.digipost.signature.jaxb.JaxbMarshaller;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An access token can be rejected before this client considers it stale, e.g. if it is revoked or if
 * this host's clock runs ahead of the token endpoint's. The rejected token must then be discarded,
 * and the request retried when it is safe to do so.
 */
@WireMockTest
class ClientConfigurationRejectedTokenTest {

    private static final JaxbMarshaller responseMarshaller = JaxbMarshaller.ForResponsesOfAllApis.singleton();

    private static final String TOKEN_PATH = "/token";
    private static final String JOBS_PATH = ".*/portal/signature-jobs";
    private static final String PADES_PATH = ".*/pades$";
    private static final String SIGNER_PATH = ".*/signers/.*";

    private final ServiceEnvironment unitTestEnv;
    private final ClientConfiguration.Builder configBuilder;

    ClientConfigurationRejectedTokenTest(WireMockRuntimeInfo wireMockInfo) {
        this.unitTestEnv = STAGING
                .withServiceUrl(URI.create(wireMockInfo.getHttpBaseUrl()))
                .withTokenEndpoint(URI.create(wireMockInfo.getHttpBaseUrl() + TOKEN_PATH));
        this.configBuilder = ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(unitTestEnv)
                .defaultSender(new Sender("123456789"))
                .jwtAuthentication(JwtAuthConfig.forClient("my-client-id", BrokerId.of("555444")));
    }


    @Test
    void retriesASafeRequestOnceWithAFreshTokenWhenTheFirstIsRejected() throws IOException {
        stubTwoTokensInSequence();

        String scenario = "rejected token";
        givenThat(get(urlPathMatching(PADES_PATH)).inScenario(scenario).whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(401).withBody("token rejected"))
                .willSetStateTo("token rejected once"));
        givenThat(get(urlPathMatching(PADES_PATH)).inScenario(scenario).whenScenarioStateIs("token rejected once")
                .willReturn(ok("a PDF")));

        PortalClient client = new PortalClient(configBuilder.build());
        try (InputStream pades = client.getPAdES(PAdESReference.of(
                URI.create(unitTestEnv.signatureServiceRootUrl() + "/pades")))) {
            assertThat(IOUtils.toString(pades, UTF_8), is("a PDF"));
        }

        // Transparent to the caller: two attempts were made, the second with the replacement token.
        assertThat(authorizationHeadersOf(findAll(getRequestedFor(urlPathMatching(PADES_PATH)))),
                contains("Bearer first-token", "Bearer second-token"));
        verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void doesNotRetryCreatingASignatureJobButStillDiscardsTheRejectedToken() {
        stubTwoTokensInSequence();
        givenThat(post(urlPathMatching(JOBS_PATH)).willReturn(aResponse().withStatus(401).withBody("token rejected")));

        PortalClient client = new PortalClient(configBuilder.build());

        // The rejected request is not retried, as sending a signature job twice is not safe.
        assertThrows(SignatureException.class, () -> client.create(aPortalJob()));
        assertThrows(SignatureException.class, () -> client.create(aPortalJob()));

        // ... but the token was discarded, so the second attempt used a newly acquired one.
        assertThat(authorizationHeadersOf(findAll(postRequestedFor(urlPathMatching(JOBS_PATH)))),
                contains("Bearer first-token", "Bearer second-token"));
        verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void keepsUsingTheCachedTokenWhenRequestsSucceed() {
        stubTwoTokensInSequence();
        givenThat(post(urlPathMatching(JOBS_PATH)).willReturn(ok(responseMarshaller.marshalToString(
                new XMLPortalSignatureJobResponse(null, 42, unitTestEnv.signatureServiceRootUrl())))));

        PortalClient client = new PortalClient(configBuilder.build());
        client.create(aPortalJob());
        client.create(aPortalJob());

        assertThat(authorizationHeadersOf(findAll(postRequestedFor(urlPathMatching(JOBS_PATH)))),
                contains("Bearer first-token", "Bearer first-token"));
        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    /**
     * Requesting a new redirect URL is a POST whose entity <em>is</em> repeatable, unlike creating a
     * signature job. It must still not be retried, as it is not a safe request to repeat.
     */
    @Test
    void doesNotRetryAnUnsafeRequestEvenWhenItsBodyCouldBeResent() {
        stubTwoTokensInSequence();
        givenThat(post(urlPathMatching(SIGNER_PATH)).willReturn(aResponse().withStatus(401).withBody("token rejected")));

        DirectClient client = new DirectClient(configBuilder.build());
        WithSignerUrl signerUrl = WithSignerUrl.of(
                URI.create(unitTestEnv.signatureServiceRootUrl() + "/direct/signature-jobs/1/signers/1"));

        assertThrows(SignatureException.class, () -> client.requestNewRedirectUrl(signerUrl));

        verify(1, postRequestedFor(urlPathMatching(SIGNER_PATH)));
        assertThat(authorizationHeadersOf(findAll(postRequestedFor(urlPathMatching(SIGNER_PATH)))),
                contains("Bearer first-token"));
    }

    @Test
    void aPersistentlyRejectedTokenIsRetriedOnlyOncePerRequest() {
        stubTwoTokensInSequence();
        givenThat(get(urlPathMatching(PADES_PATH)).willReturn(aResponse().withStatus(401).withBody("token rejected")));

        PortalClient client = new PortalClient(configBuilder.build());
        PAdESReference pades = PAdESReference.of(URI.create(unitTestEnv.signatureServiceRootUrl() + "/pades"));

        assertThrows(SignatureException.class, () -> client.getPAdES(pades));

        // One initial attempt and exactly one retry, rather than looping.
        verify(2, getRequestedFor(urlPathMatching(PADES_PATH)));
    }


    private static List<String> authorizationHeadersOf(List<LoggedRequest> requests) {
        return requests.stream()
                .map(request -> request.getHeader("Authorization"))
                .collect(Collectors.toList());
    }

    private static void stubTwoTokensInSequence() {
        String scenario = "two tokens";
        givenThat(post(urlEqualTo(TOKEN_PATH)).inScenario(scenario).whenScenarioStateIs(STARTED)
                .willReturn(okJson("{\"access_token\":\"first-token\",\"expires_in\":3600}"))
                .willSetStateTo("first token acquired"));
        givenThat(post(urlEqualTo(TOKEN_PATH)).inScenario(scenario).whenScenarioStateIs("first token acquired")
                .willReturn(okJson("{\"access_token\":\"second-token\",\"expires_in\":3600}")));
    }

    private static PortalJob aPortalJob() {
        return PortalJob.builder("Job title",
                    PortalDocument.builder("Document title", "contents".getBytes(UTF_8)).build(),
                    PortalSigner.identifiedByEmail("jane@example.com").build())
                .build();
    }

}
