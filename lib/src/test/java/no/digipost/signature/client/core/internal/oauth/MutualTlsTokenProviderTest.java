package no.digipost.signature.client.core.internal.oauth;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import no.digipost.signature.client.ServiceEnvironment;
import no.digipost.signature.client.core.exceptions.AccessTokenException;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.security.oauth.ClientId;
import no.digipost.signature.client.security.oauth.OAuthConfiguration;
import no.digipost.signature.client.security.oauth.Scope;
import no.digipost.time.ControllableClock;
import no.digipost.wiremock.ResponseBlocker;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.time.Duration.ofSeconds;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.digipost.signature.client.core.internal.oauth.MutualTlsTokenProvider.CachedAccessToken.REFRESH_MARGIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

@Timeout(5)
class MutualTlsTokenProviderTest {

    @RegisterExtension
    private final static ResponseBlocker RESPONSE_BLOCKER = new ResponseBlocker();

    @RegisterExtension
    private final static WireMockExtension WIREMOCK = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().extensions(RESPONSE_BLOCKER))
            .build();

    @BeforeAll
    static void setDefaultWireMock() {
        WireMock.configureFor(WIREMOCK.getPort());
    }

    private static final String TOKEN_PATH = "/token";
    private static final Instant NOW = Instant.parse("2026-08-17T12:00:00Z");

    private final OAuthConfiguration oAuthConfiguration;
    private final ControllableClock clock = ControllableClock.freezedAt(NOW, UTC);
    private final HttpClient httpClient = HttpClientBuilder.create().build();

    MutualTlsTokenProviderTest(WireMockRuntimeInfo wireMockInfo) {
        this.oAuthConfiguration = new OAuthConfiguration(
                URI.create(wireMockInfo.getHttpBaseUrl() + TOKEN_PATH),
                ServiceEnvironment.OAuthDefaults.PRODUCTION.resource(),
                new ClientId("my-client-id"),
                Scope.fromAccountId(555444));
    }

    private MutualTlsTokenProvider tokenProvider() {
        return new MutualTlsTokenProvider(oAuthConfiguration, httpClient, clock);
    }


    @Test
    void acquiresAnAccessToken() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));

        assertThat(tokenProvider().getToken(), is("a-token"));
    }

    @Test
    void sendsAClientCredentialsGrantWithScopeAndResource() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));

        tokenProvider().getToken();

        verify(postRequestedFor(urlEqualTo(TOKEN_PATH))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withRequestBody(containing("grant_type=client_credentials"))
                .withRequestBody(containing("client_id=my-client-id"))
                .withRequestBody(containing("scope=signering%3A555444"))
                .withRequestBody(containing("resource=https%3A%2F%2Fapi.signering.posten.no")));
    }

    @Test
    void returnsTheExistingTokenWhileRefreshing() {
        stubTwoTokensInSequence();
        MutualTlsTokenProvider tokenProvider = tokenProvider();

        assertThat(tokenProvider, where(MutualTlsTokenProvider::getToken, is("first-token")));

        clock.timePasses(Duration.ofHours(1).minus(REFRESH_MARGIN));
        RESPONSE_BLOCKER.block();

        CompletableFuture<String> secondToken = CompletableFuture.supplyAsync(tokenProvider::getToken);
        RESPONSE_BLOCKER.awaitUntilBlocking();
        assertThat(tokenProvider, where(MutualTlsTokenProvider::getToken, is("first-token")));

        RESPONSE_BLOCKER.release();
        assertThat(secondToken.join(), is("second-token"));
    }

    @Test
    void cachesTheTokenInsteadOfAcquiringOnePerCall() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        assertThat(tokenProvider.getToken(), is("a-token"));
        assertThat(tokenProvider.getToken(), is("a-token"));
        clock.timePasses(Duration.ofHours(1).minus(REFRESH_MARGIN).minus(Duration.ofSeconds(1)));
        assertThat(tokenProvider.getToken(), is("a-token"));

        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void acquiresANewTokenOnceTheCachedOneIsWithinTheRefreshMargin() {
        stubTwoTokensInSequence();

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        assertThat(tokenProvider.getToken(), is("first-token"));

        // Still outside the refresh margin, so the first token is reused.
        clock.timePasses(Duration.ofHours(1).minus(REFRESH_MARGIN).minus(Duration.ofSeconds(1)));
        assertThat(tokenProvider.getToken(), is("first-token"));

        // Within the refresh margin, so a new token is acquired before the first expires
        clock.timePasses(ofSeconds(2));
        assertThat(tokenProvider.getToken(), is("second-token"));

        verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void concurrentCallersShareTheOneAcquiredToken() throws Exception {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}").withFixedDelay(200)));

        MutualTlsTokenProvider tokenProvider = tokenProvider();

        int callers = 8;
        ExecutorService threadPool = Executors.newFixedThreadPool(callers);
        CountDownLatch releaseAllCallers = new CountDownLatch(1);
        try {
            List<Future<String>> acquiredTokens = new ArrayList<>();
            for (int caller = 0; caller < callers; caller++) {
                acquiredTokens.add(threadPool.submit(() -> {
                    releaseAllCallers.await();
                    return tokenProvider.getToken();
                }));
            }
            releaseAllCallers.countDown();
            for (Future<String> acquiredToken : acquiredTokens) {
                assertThat(acquiredToken.get(30, SECONDS), is("a-token"));
            }
        } finally {
            threadPool.shutdownNow();
        }

        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void invalidatingTheCachedTokenMakesTheNextCallAcquireANewOne() {
        stubTwoTokensInSequence();

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        assertThat(tokenProvider.getToken(), is("first-token"));

        tokenProvider.invalidate();

        assertThat(tokenProvider.getToken(), is("second-token"));
        verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }


    @Test
    void invalidatingWhenNoTokenIsCachedIsHarmless() {
        stubTwoTokensInSequence();

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        tokenProvider.invalidate();

        assertThat(tokenProvider.getToken(), is("first-token"));
        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    /**
     * A token which is already stale on arrival is handed out, but not cached, and warned about.
     */
    @Test
    void aTokenExpiringWithinTheRefreshMarginIsUsedButNotCached() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"a-short-lived-token\",\"expires_in\":10}")));

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        assertThat(tokenProvider.getToken(), is("a-short-lived-token"));
        assertThat(tokenProvider.getToken(), is("a-short-lived-token"));

        verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void anExpiresInWhichGivesTheTokenNoLifetimeIsRejected() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":0}")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("not a usable value")));
    }

    @Test
    void aNegativeExpiresInIsRejected() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":-1}")));

        assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
    }

    @Test
    void anExpiresInWhichCannotBeAddedToTheCurrentTimeIsRejected() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":" + Long.MAX_VALUE + "}")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("not a usable value")));
    }

    @Test
    void aResponseWithoutExpiresInIsReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"an-opaque-token\"}")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("expires_in")));
    }

    @Test
    void aNonIntegerExpiresInIsNotAccepted() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"an-opaque-token\",\"expires_in\":\"3600\"}")));

        assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
    }

    @Test
    void aNullExpiresInIsReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"an-opaque-token\",\"expires_in\":null}")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("expires_in")));
    }

    @Test
    void aFractionalExpiresInIsNotAccepted() {
        givenThat(post(urlEqualTo(TOKEN_PATH))
                .willReturn(okJson("{\"access_token\":\"an-opaque-token\",\"expires_in\":3600.5}")));

        assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
    }

    @Test
    void ignoresOtherFieldsOfTheResponseIncludingNestedOnes() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson(
                "{\"token_type\":\"Bearer\"," +
                "\"nested\":{\"access_token\":\"decoy\",\"deeper\":[1,{\"expires_in\":1}]}," +
                "\"access_token\":\"a-token\"," +
                "\"expires_in\":3600}")));

        assertThat(tokenProvider().getToken(), is("a-token"));
    }

    @Test
    void anUnsuccessfulResponseIncludesTheStatusAndBody() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withStatus(401)
                .withBody("{\"error\":\"invalid_client\"}")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("401")));
        assertThat(thrown, where(Throwable::getMessage, containsString("invalid_client")));
    }

    @Test
    void anUnsuccessfulResponseWithoutABodyIsStillReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withStatus(503)));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("503")));
    }

    @Test
    void aResponseWhichIsNotJsonIsReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withStatus(200)
                .withBody("<html><body>Gateway error</body></html>")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("as JSON")));
    }

    @Test
    void aResponseWithoutAnAccessTokenIsReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"token_type\":\"Bearer\",\"expires_in\":60}")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("access_token")));
    }

    @Test
    void aNullAccessTokenIsReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":null,\"expires_in\":60}")));

        AccessTokenException thrown = assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
        assertThat(thrown, where(Throwable::getMessage, containsString("access_token")));
    }

    @Test
    void anEmptyAccessTokenIsReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"\",\"expires_in\":60}")));

        assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
    }

    @Test
    void aTokenEndpointWhichCannotBeReachedIsReportedAsAnIoProblem() {
        OAuthConfiguration unreachable = new OAuthConfiguration(URI.create("http://localhost:1/token"), emptyList());

        assertThrows(HttpIOException.class,
                () -> new MutualTlsTokenProvider(unreachable, httpClient, clock).getToken());
    }


    private static void stubTwoTokensInSequence() {
        String scenario = "two tokens";
        givenThat(post(urlEqualTo(TOKEN_PATH)).inScenario(scenario).whenScenarioStateIs(STARTED)
                .willReturn(okJson("{\"access_token\":\"first-token\",\"expires_in\":3600}"))
                .willSetStateTo("first token acquired"));
        givenThat(post(urlEqualTo(TOKEN_PATH)).inScenario(scenario).whenScenarioStateIs("first token acquired")
                .willReturn(okJson("{\"access_token\":\"second-token\",\"expires_in\":3600}")));
    }

}
