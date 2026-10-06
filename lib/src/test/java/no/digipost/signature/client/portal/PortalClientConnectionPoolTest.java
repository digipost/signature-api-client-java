package no.digipost.signature.client.portal;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.ServiceEnvironment;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.matching.UrlPattern.ANY;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

@WireMockTest
class PortalClientConnectionPoolTest {

    private final ServiceEnvironment unitTestEnv;
    private final ClientConfiguration.Builder configBuilder;

    PortalClientConnectionPoolTest(WireMockRuntimeInfo wireMockInfo) {
        this.unitTestEnv = STAGING.withServiceUrl(URI.create(wireMockInfo.getHttpBaseUrl()));
        this.configBuilder = ClientConfiguration.builder(CLIENT_KEYSTORE)
                .serviceEnvironment(unitTestEnv)
                .defaultSender(new Sender("123456789"));
    }

    @Test
    @Timeout(value = 3, unit = SECONDS)
    void exhaustingConnectionPoolWhenMissingResourceManagement() {

        int connectionPoolSize = 15;
        PortalClient client = new PortalClient(configBuilder
                .timeoutsForDocumentDownloads(t -> t.connectionRequestTimeout(ofMillis(100)))
                .connectionPoolForDocumentDownloads(pool -> pool.maxTotalConnectionsInPool(connectionPoolSize))
                .build());

        givenThat(get(ANY).willReturn(ok()));

        PAdESReference padesReference = PAdESReference.of(URI.create(unitTestEnv.signatureServiceRootUrl() + "/pades"));
        HttpIOException thrown = assertThrows(HttpIOException.class, () -> Stream.generate(() -> padesReference).forEach(client::getPAdES));

        assertAll(
                () -> assertThat(thrown, where(Throwable::getCause, instanceOf(ConnectionRequestTimeoutException.class))),
                () -> assertThat(thrown, where(Throwable::getMessage, containsStringIgnoringCase("could not be obtained from the connection pool"))),
                () -> assertThat(thrown, where(Throwable::getMessage, containsStringIgnoringCase("missing resource management of responses")))
            );

        verify(connectionPoolSize, newRequestPattern(GET, urlPathMatching(".*/pades$")));
    }

}
