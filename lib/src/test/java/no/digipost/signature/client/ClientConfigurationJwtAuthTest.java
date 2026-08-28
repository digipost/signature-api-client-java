package no.digipost.signature.client;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signature.client.portal.PortalClient;
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;
import no.digipost.signature.client.security.BrokerId;
import no.digipost.signature.client.security.JwtAuthConfig;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import no.digipost.signature.jaxb.JaxbMarshaller;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

@WireMockTest
class ClientConfigurationJwtAuthTest {

    private static final JaxbMarshaller responseMarshaller = JaxbMarshaller.ForResponsesOfAllApis.singleton();

    private static final String TOKEN_PATH = "/token";

    /**
     * Matches the token endpoint whether or not the request is sent through a proxy, as a proxied
     * request states the whole URI rather than just the path.
     */
    private static final String ANY_TOKEN_PATH = ".*" + TOKEN_PATH + "$";

    private static final String JOBS_PATH = ".*/portal/signature-jobs";

    private final ServiceEnvironment unitTestEnv;
    private final JwtAuthConfig jwtAuthConfig;
    private final ClientConfiguration.Builder configBuilder;
    private final URI wireMockBaseUri;

    ClientConfigurationJwtAuthTest(WireMockRuntimeInfo wireMockInfo) {
        this.wireMockBaseUri = URI.create(wireMockInfo.getHttpBaseUrl());
        this.unitTestEnv = STAGING
                .withServiceUrl(URI.create(wireMockInfo.getHttpBaseUrl()))
                .withTokenEndpoint(URI.create(wireMockInfo.getHttpBaseUrl() + TOKEN_PATH));
        this.jwtAuthConfig = JwtAuthConfig.forClient("my-client-id", BrokerId.of("555444"));
        this.configBuilder = ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(unitTestEnv)
                .defaultSender(new Sender("123456789"));
    }


    @Test
    void requestsAnAccessTokenForTheScopeOfTheConfiguredBroker() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        new PortalClient(configBuilder.jwtAuthentication(jwtAuthConfig).build()).create(aPortalJob());

        verify(postRequestedFor(urlEqualTo(TOKEN_PATH))
                .withRequestBody(containing("scope=signering-api%3A555444")));
    }

    /**
     * A broker acquires its access tokens as itself, and may act on behalf of several organizations.
     * Which sender a job is for is stated in the job itself, and must not influence the scope the
     * token is requested for.
     */
    @Test
    void theScopeIsTheBrokersRegardlessOfWhichSenderAJobIsFor() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        PortalClient client = new PortalClient(configBuilder.jwtAuthentication(jwtAuthConfig).build());
        client.create(aPortalJobFor(new Sender("999888777")));

        verify(postRequestedFor(urlEqualTo(TOKEN_PATH))
                .withRequestBody(containing("scope=signering-api%3A555444")));
        assertThat(tokenRequestParameter("scope"), is("signering-api:555444"));
    }

    /**
     * The scope is the broker's, so nothing about acquiring an access token depends on a sender. A
     * broker specifying the sender per job does not need a default one.
     */
    @Test
    void doesNotRequireADefaultSender() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        ClientConfiguration withoutDefaultSender = ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(unitTestEnv)
                .jwtAuthentication(jwtAuthConfig)
                .build();

        new PortalClient(withoutDefaultSender).create(aPortalJobFor(new Sender("999888777")));

        verify(postRequestedFor(urlEqualTo(TOKEN_PATH))
                .withRequestBody(containing("scope=signering-api%3A555444")));
    }

    @Test
    void requestsAnAccessTokenForTheApiOfTheServiceEnvironment() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        new PortalClient(configBuilder.jwtAuthentication(jwtAuthConfig).build()).create(aPortalJob());

        assertThat(tokenRequestParameter("resource"), is(unitTestEnv.signatureServiceRootUrl().toString()));
    }

    @Test
    void requiresTheServiceEnvironmentToKnowATokenEndpoint() {
        ClientConfiguration.Builder customEnvironmentWithoutTokenEndpoint = ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(new ServiceEnvironment(
                        "Custom", unitTestEnv.signatureServiceRootUrl(), unitTestEnv.certificatePaths()))
                .defaultSender(new Sender("123456789"))
                .jwtAuthentication(jwtAuthConfig);

        ConfigurationException thrown = assertThrows(
                ConfigurationException.class, customEnvironmentWithoutTokenEndpoint::build);
        assertThat(thrown, where(Throwable::getMessage, containsString("withTokenEndpoint")));
    }

    @Test
    void sendsTheAccessTokenAsABearerTokenOnApiRequests() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        new PortalClient(configBuilder.jwtAuthentication(jwtAuthConfig).build()).create(aPortalJob());

        verify(postRequestedFor(urlPathMatching(JOBS_PATH))
                .withHeader("Authorization", equalTo("Bearer a-token")));
    }

    @Test
    void documentDownloadsAlsoCarryTheAccessToken() throws IOException {
        stubTokenEndpoint("a-token");
        givenThat(get(urlPathMatching(".*/pades$")).willReturn(ok("a PDF")));

        PortalClient client = new PortalClient(configBuilder.jwtAuthentication(jwtAuthConfig).build());
        PAdESReference padesReference = PAdESReference.of(URI.create(unitTestEnv.signatureServiceRootUrl() + "/pades"));

        try (InputStream pades = client.getPAdES(padesReference)) {
            assertThat(IOUtils.toString(pades, UTF_8), is("a PDF"));
        }

        verify(getRequestedFor(urlPathMatching(".*/pades$"))
                .withHeader("Authorization", equalTo("Bearer a-token")));
    }

    @Test
    void acquiresTheAccessTokenOnlyOnceForSeveralApiRequests() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        PortalClient client = new PortalClient(configBuilder.jwtAuthentication(jwtAuthConfig).build());
        client.create(aPortalJob());
        client.create(aPortalJob());

        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
        verify(2, postRequestedFor(urlPathMatching(JOBS_PATH)));
    }

    @Test
    void sendsNoAuthorizationHeaderAndAcquiresNoTokenWhenJwtAuthenticationIsNotConfigured() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        new PortalClient(configBuilder.build()).create(aPortalJob());

        verify(0, postRequestedFor(urlEqualTo(TOKEN_PATH)));
        verify(postRequestedFor(urlPathMatching(JOBS_PATH)).withHeader("Authorization", absent()));
    }

    /**
     * A proxy is configured for the client as a whole, and acquiring access tokens must go through it
     * as well. In a proxied environment the token endpoint would otherwise be unreachable.
     */
    @Test
    void routesTokenRequestsThroughTheConfiguredProxy() {
        givenThat(post(urlPathMatching(ANY_TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));
        stubCreateJob();

        // Nothing is listening on port 1, so the token endpoint is only reachable via the proxy.
        ClientConfiguration proxiedConfig = ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(unitTestEnv.withTokenEndpoint(URI.create("http://localhost:1" + TOKEN_PATH)))
                .defaultSender(new Sender("123456789"))
                .proxyHost(wireMockBaseUri)
                .jwtAuthentication(jwtAuthConfig)
                .build();

        new PortalClient(proxiedConfig).create(aPortalJob());

        verify(postRequestedFor(urlPathMatching(ANY_TOKEN_PATH)));
    }

    @Test
    void stillSendsTheMandatoryUserAgentToTheTokenEndpoint() {
        stubTokenEndpoint("a-token");
        stubCreateJob();

        new PortalClient(configBuilder.includeInUserAgent("My Corporation").jwtAuthentication(jwtAuthConfig).build())
                .create(aPortalJob());

        verify(postRequestedFor(urlEqualTo(TOKEN_PATH))
                .withHeader("User-Agent", containing("My Corporation")));
    }


    /**
     * The decoded value of a single form parameter from the one expected token request, so that
     * assertions can be made on the actual value rather than on its url-encoded form.
     */
    private static String tokenRequestParameter(String name) {
        List<LoggedRequest> tokenRequests = findAll(postRequestedFor(urlEqualTo(TOKEN_PATH)));
        assertThat(tokenRequests, hasSize(1));
        return Stream.of(tokenRequests.get(0).getBodyAsString().split("&"))
                .map(parameter -> parameter.split("=", 2))
                .filter(parameter -> parameter[0].equals(name))
                .map(parameter -> urlDecode(parameter[1]))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No '" + name + "' parameter in the token request: " + tokenRequests.get(0).getBodyAsString()));
    }

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private static void stubTokenEndpoint(String accessToken) {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"" + accessToken + "\",\"expires_in\":3600}")));
    }

    private void stubCreateJob() {
        givenThat(post(urlPathMatching(JOBS_PATH)).willReturn(ok(responseMarshaller.marshalToString(
                new XMLPortalSignatureJobResponse(null, 42, unitTestEnv.signatureServiceRootUrl())))));
    }

    private static PortalJob aPortalJob() {
        return aPortalJobBuilder().build();
    }

    private static PortalJob aPortalJobFor(Sender sender) {
        return aPortalJobBuilder().withSender(sender).build();
    }

    private static PortalJob.Builder aPortalJobBuilder() {
        return PortalJob.builder("Job title",
                    PortalDocument.builder("Document title", "contents".getBytes(UTF_8)).build(),
                    PortalSigner.identifiedByEmail("jane@example.com").build());
    }

}
