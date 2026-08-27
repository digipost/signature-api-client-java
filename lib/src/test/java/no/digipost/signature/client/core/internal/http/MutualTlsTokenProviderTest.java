package no.digipost.signature.client.core.internal.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.digipost.signature.client.core.exceptions.AccessTokenException;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
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
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

@WireMockTest
class MutualTlsTokenProviderTest {

    private static final String TOKEN_PATH = "/token";
    private static final Instant NOW = Instant.parse("2026-08-17T12:00:00Z");

    private final AccessTokenRequest accessTokenRequest;
    private final MutableClock clock = new MutableClock(NOW);
    private final HttpClient httpClient = HttpClientBuilder.create().build();

    MutualTlsTokenProviderTest(WireMockRuntimeInfo wireMockInfo) {
        this.accessTokenRequest = new AccessTokenRequest(
                URI.create(wireMockInfo.getHttpBaseUrl() + TOKEN_PATH),
                "my-client-id",
                "signering-api:123456789",
                "https://api.signering.posten.no/api");
    }

    private MutualTlsTokenProvider tokenProvider() {
        return new MutualTlsTokenProvider(accessTokenRequest, httpClient, clock);
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
                .withRequestBody(containing("scope=signering-api%3A123456789"))
                .withRequestBody(containing("resource=https%3A%2F%2Fapi.signering.posten.no%2Fapi")));
    }

    @Test
    void cachesTheTokenInsteadOfAcquiringOnePerCall() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"a-token\",\"expires_in\":3600}")));

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        assertThat(tokenProvider.getToken(), is("a-token"));
        assertThat(tokenProvider.getToken(), is("a-token"));
        clock.advance(ofSeconds(3600 - MutualTlsTokenProvider.REFRESH_MARGIN_SECONDS - 1));
        assertThat(tokenProvider.getToken(), is("a-token"));

        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void acquiresANewTokenOnceTheCachedOneIsWithinTheRefreshMargin() {
        stubTwoTokensInSequence();

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        assertThat(tokenProvider.getToken(), is("first-token"));

        // Still outside the refresh margin, so the first token is reused.
        clock.advance(ofSeconds(3600 - MutualTlsTokenProvider.REFRESH_MARGIN_SECONDS - 1));
        assertThat(tokenProvider.getToken(), is("first-token"));

        // Now within the refresh margin, and a new token is acquired even though the first has not
        // technically expired yet.
        clock.advance(ofSeconds(2));
        assertThat(tokenProvider.getToken(), is("second-token"));

        verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    /**
     * Several threads may need an access token before any of them has one. Acquiring a token for each
     * of them would be both wasteful and needless load on the token endpoint.
     */
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

        tokenProvider.invalidate("first-token");

        assertThat(tokenProvider.getToken(), is("second-token"));
        verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    /**
     * Two requests may fail with the same rejected token, or one may fail while another thread has
     * already replaced it. Invalidating a token which is no longer the cached one must not throw away
     * the replacement.
     */
    @Test
    void invalidatingATokenWhichIsNoLongerTheCachedOneKeepsTheCachedOne() {
        stubTwoTokensInSequence();

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        assertThat(tokenProvider.getToken(), is("first-token"));

        tokenProvider.invalidate("a-token-acquired-before-this-one");

        assertThat(tokenProvider.getToken(), is("first-token"));
        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void invalidatingWhenNoTokenIsCachedIsHarmless() {
        stubTwoTokensInSequence();

        MutualTlsTokenProvider tokenProvider = tokenProvider();
        tokenProvider.invalidate("a-token-which-was-never-acquired");

        assertThat(tokenProvider.getToken(), is("first-token"));
        verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    /**
     * A token which is already stale when it arrives is still handed out, as it is the only one there
     * is, but it can not be cached. This is warned about, as it means the token endpoint is called for
     * every single request.
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

    /**
     * A lifetime this large would overflow when added to the current time, and must be reported as the
     * unusable response it is, not as an arithmetic error.
     */
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
    void anEmptyAccessTokenIsReported() {
        givenThat(post(urlEqualTo(TOKEN_PATH)).willReturn(okJson("{\"access_token\":\"\",\"expires_in\":60}")));

        assertThrows(AccessTokenException.class, () -> tokenProvider().getToken());
    }

    @Test
    void aTokenEndpointWhichCannotBeReachedIsReportedAsAnIoProblem() {
        AccessTokenRequest unreachable = new AccessTokenRequest(
                URI.create("http://localhost:1/token"), "my-client-id", "a-scope", "https://api.example.com");

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

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration amount) {
            this.now = this.now.plus(amount);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

}
