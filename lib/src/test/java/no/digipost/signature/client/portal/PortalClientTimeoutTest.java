package no.digipost.signature.client.portal;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.ServiceEnvironment;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.jaxb.JaxbMarshaller;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.matching.UrlPattern.ANY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

@WireMockTest
class PortalClientTimeoutTest {

    private static final JaxbMarshaller responseMarshaller = JaxbMarshaller.ForResponsesOfAllApis.singleton();

    private final ServiceEnvironment unitTestEnv;
    private final ClientConfiguration.Builder configBuilder;

    PortalClientTimeoutTest(WireMockRuntimeInfo wireMockInfo) {
        this.unitTestEnv = STAGING.withServiceUrl(URI.create(wireMockInfo.getHttpBaseUrl()));
        this.configBuilder = ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(unitTestEnv)
                .defaultSender(new Sender("123456789"));
    }

    @Test
    void createJobWithNoTimeoutExceeded() {
        givenThat(post(ANY).willReturn(ok(responseMarshaller.marshalToString(
                new XMLPortalSignatureJobResponse(null, 42, unitTestEnv.signatureServiceRootUrl())))));

        PortalJobResponse response = new PortalClient(configBuilder.build()).create(aPortalJob());
        assertThat(response, where(PortalJobResponse::getSignatureJobId, is(42L)));
    }

    @Test
    void createJobWhereResponseArrivalExceedsTimeouts() {

        ClientConfiguration shortResponseArrivalTimeout =
                configBuilder.timeouts(t -> t.responseArrivalTimeout(ofMillis(100))).build();

        givenThat(post(ANY).willReturn(serverError().withFixedDelay(150)
                .withBody("Client should time out before seeing this response")));

        HttpIOException timeoutException = assertThrows(HttpIOException.class,
                () -> new PortalClient(shortResponseArrivalTimeout).create(aPortalJob()));
        assertThat(timeoutException, where(Throwable::getMessage, matchesRegex(".*[Rr]ead timed out.*")));
    }

    @Test
    void exceededConnectTimeout() {
        ClientConfiguration nonRespondingHost = configBuilder
                .serviceEnvironment(env -> env.withServiceUrl(URI.create("http://10.255.255.1:49200"))) //non-routable IP address
                .timeouts(t -> t.connectTimeout(ofMillis(20)))
                .build();

        HttpIOException timeoutException = assertThrows(HttpIOException.class,
                () -> new PortalClient(nonRespondingHost).create(aPortalJob()));
        assertThat(timeoutException, where(Throwable::getMessage, matchesRegex(".*[Cc]onnect timed out.*")));
    }

    @Test
    void slowPadesDownload() throws IOException {

        ClientConfiguration.Builder shortResponseArrivalTimeout = configBuilder
                .timeouts(t -> t.allTimeouts(ofMillis(150)))
                .timeoutsForDocumentDownloads(t -> t.responseArrivalTimeout(ofMillis(100)));

        PortalClient client = new PortalClient(shortResponseArrivalTimeout.build());

        givenThat(post(ANY).willReturn(ok(responseMarshaller.marshalToString(
                new XMLPortalSignatureJobResponse(null, 42, unitTestEnv.signatureServiceRootUrl())))));

        assertThat(client.create(aPortalJob()), isA(PortalJobResponse.class));

        givenThat(get(urlPathMatching(".*/pades$")).willReturn(ok("a PDF").withFixedDelay(250)));

        PAdESReference padesReference = PAdESReference.of(URI.create(unitTestEnv.signatureServiceRootUrl() + "/pades"));
        HttpIOException firstAttempt = assertThrows(HttpIOException.class, () -> client.getPAdES(padesReference));
        HttpIOException secondAttempt = assertThrows(HttpIOException.class, () -> client.getPAdES(padesReference));
        assertAll(Stream.of(firstAttempt, secondAttempt)
                .map(e -> () -> assertThat(e, where(Throwable::getMessage, matchesRegex(".*[Rr]ead timed out.*")))));

        PortalClient clientWithIncreasedTimeout = new PortalClient(shortResponseArrivalTimeout
                .timeoutsForDocumentDownloads(t -> t.responseArrivalTimeout(ofSeconds(1)))
                .build());

        try (InputStream pades = clientWithIncreasedTimeout.getPAdES(padesReference)) {
            assertThat(IOUtils.toString(pades, UTF_8), is("a PDF"));
        }

    }



    private static PortalJob aPortalJob() {
        return PortalJob.builder("Job title",
                    PortalDocument.builder("Document title", "contents".getBytes(UTF_8)).build(),
                    PortalSigner.identifiedByEmail("jane@example.com").build())
                .build();
    }

}
