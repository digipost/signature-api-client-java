package no.digipost.signature.client.core.internal.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import no.digipost.signature.client.core.exceptions.AccessTokenException;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientBuilderConfigurer;
import no.digipost.signature.client.core.internal.configuration.Configurer;
import no.digipost.signature.client.core.internal.http.StatusCode;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.client.security.oauth.OAuthConfiguration;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static no.digipost.signature.client.core.internal.http.StatusCode.Family.SUCCESSFUL;
import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;

/**
 * Acquires OAuth 2.0 access tokens with the <em>client credentials</em> grant over mTLS. Tokens are
 * cached, and replaced on the first {@link #getToken()} within the duration of
 * {@link CachedAccessToken#REFRESH_MARGIN} of expiry.
 */
public class MutualTlsTokenProvider {

    private static final Logger LOG = Logger.getLogger(MutualTlsTokenProvider.class.getName());

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .build();


    private final URI tokenEndpointUrl;
    private final List<NameValuePair> tokenRequestParameters;
    private final HttpClient tokenClient;
    private final Clock clock;

    private final ReentrantLock refreshLock = new ReentrantLock();
    private volatile CachedAccessToken cachedToken = CachedAccessToken.INITIAL_PENDING_REFRESH;


    /**
     * Create a token provider which presents the client certificate of the given {@link KeyStoreConfig}.
     * The token endpoint (mIdP) is validated against the JVM's default trust store, not the trust
     * configuration for authenticating to the API with the organization certificate.
     *
     * @param commonHttpClientConfiguration shared with the API clients, e.g. User-Agent and proxy
     */
    public static MutualTlsTokenProvider create(
            OAuthConfiguration oAuthConfiguration,
            KeyStoreConfig keyStoreConfig,
            Configurer<HttpClientBuilder> commonHttpClientConfiguration,
            Clock clock
    ) {
        Configurer<HttpClientBuilder> tokenClientConfiguration = new ApacheHttpClientBuilderConfigurer()
                .connectionManager(connectionManager -> connectionManager
                        .setTlsSocketStrategy(new DefaultClientTlsStrategy(mutualTlsSslContext(keyStoreConfig))))
                .socketTimeout(Duration.ofSeconds(5))
                .connectTimeout(Duration.ofSeconds(5))
                .connectionRequestTimeout(Duration.ofSeconds(5))
                .responseArrivalTimeout(Duration.ofSeconds(10));

        HttpClientBuilder tokenClientBuilder = HttpClientBuilder.create();
        commonHttpClientConfiguration.andThen(tokenClientConfiguration).applyTo(tokenClientBuilder);

        return new MutualTlsTokenProvider(oAuthConfiguration, tokenClientBuilder.build(), clock);
    }

    /**
     * @param tokenClient the HTTP client is required to present the client certificate
     *
     * @see #create(OAuthConfiguration, KeyStoreConfig, Configurer, Clock)
     */
    public MutualTlsTokenProvider(OAuthConfiguration tokenRequestConfiguration, HttpClient tokenClient, Clock clock) {
        this.tokenEndpointUrl = tokenRequestConfiguration.tokenEndpoint();
        this.tokenClient = requireNonNull(tokenClient, "token endpoint HTTP client");
        this.clock = requireNonNull(clock, "clock");
        this.tokenRequestParameters = tokenRequestConfiguration.requestParameters().stream()
                .map(requestParam -> requestParam.to(BasicNameValuePair::new))
                .collect(toList());
    }


    /**
     * The cached access token, or a new one if it is absent or about to expire.
     */
    public String getToken() {
        CachedAccessToken current = cachedToken;
        if (!current.isRefreshDueAt(clock.instant())) {
            return current.token;
        }

        if (refreshLock.tryLock()) {
            try {
                current = cachedToken;
                if (!current.isRefreshDueAt(clock.instant())) {
                    return current.token;
                }
                CachedAccessToken refreshed = acquireToken();
                cachedToken = refreshed;
                return refreshed.token;
            } finally {
                refreshLock.unlock();
            }
        } else if (current.isValidAt(clock.instant())) {
            return current.token;
        } else {
            refreshLock.lock();
            try {
                return cachedToken.token;
            } finally {
                refreshLock.unlock();
            }
        }
    }

    public void invalidate() {
        cachedToken = CachedAccessToken.INITIAL_PENDING_REFRESH;
    }

    private CachedAccessToken acquireToken() {
        ClassicHttpRequest request = ClassicRequestBuilder
                .post(tokenEndpointUrl)
                .addHeader(ACCEPT, APPLICATION_JSON.getMimeType())
                .setEntity(new UrlEncodedFormEntity(tokenRequestParameters, UTF_8))
                .build();

        try {
            return tokenClient.execute(request, this::handleTokenResponse);
        } catch (IOException e) {
            throw new HttpIOException(request, "Unable to acquire an access token from " + tokenEndpointUrl, e);
        }
    }

    private CachedAccessToken handleTokenResponse(ClassicHttpResponse response) throws IOException, ParseException {
        HttpEntity responseEntity = response.getEntity();
        StatusCode status = StatusCode.from(response.getCode());
        if (responseEntity == null || !status.is(SUCCESSFUL)) {
            throw new AccessTokenException(
                    "Got " + status + " from the token endpoint " + tokenEndpointUrl + ", expected a " +
                    "token in the response. The response body was: " +
                    ((responseEntity != null) ? EntityUtils.toString(responseEntity, UTF_8, 512) : "(null)"));
        }

        TokenResponse tokenResponse = readTokenResponse(responseEntity.getContent());
        Instant expiresAt = Instant.now(clock).plus(tokenResponse.expiresIn);
        LOG.fine(() -> "Acquired a new access token from " + tokenEndpointUrl + ", expiring at " + expiresAt);
        return new CachedAccessToken(tokenResponse.accessToken, expiresAt);
    }

    private TokenResponse readTokenResponse(InputStream responseBody) {
        try {
            return JSON_MAPPER.readValue(responseBody, TokenResponse.class);
        } catch (IOException e) {
            throw new AccessTokenException(
                    "Could not parse the response from the token endpoint " + tokenEndpointUrl + " as JSON, because " +
                    e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
    }

    private static SSLContext mutualTlsSslContext(KeyStoreConfig keyStoreConfig) {
        try {
            return SSLContexts.custom()
                    .loadKeyMaterial(
                            keyStoreConfig.keyStore, keyStoreConfig.privatekeyPassword.toCharArray(),
                            (aliases, socket) -> keyStoreConfig.alias)
                    .build();
        } catch (Exception e) {
            throw new KeyException(
                    "Unable to create the SSLContext used to authenticate with the token endpoint, because " +
                    e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
    }


    /**
     * The required fields of the response, validated when read, so that reading a response
     * without a non-empty {@code access_token} or an {@code expires_in} fails.
     */
    private static final class TokenResponse {

        private static final String ACCESS_TOKEN_FIELD = "access_token";
        private static final String EXPIRES_IN_FIELD = "expires_in";


        final String accessToken;
        final Duration expiresIn;

        @JsonCreator
        TokenResponse(
                @JsonProperty(ACCESS_TOKEN_FIELD) String accessToken,
                @JsonProperty(EXPIRES_IN_FIELD) Long expiresInSeconds) {
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new IllegalArgumentException(ACCESS_TOKEN_FIELD + " can not be '" + accessToken + "'");
            }
            if (expiresInSeconds == null) {
                throw new IllegalArgumentException(EXPIRES_IN_FIELD + " can not be null");
            }
            this.accessToken = accessToken;
            this.expiresIn = Duration.ofSeconds(expiresInSeconds);
        }

    }


    /**
     * Token and expiry in one immutable value, so they are never read out of sync without the lock.
     */
    public static final class CachedAccessToken {
        static final Duration SAFETY_MARGIN_FOR_EXPIRY = Duration.ofSeconds(5);
        static final Duration REFRESH_MARGIN = SAFETY_MARGIN_FOR_EXPIRY.plus(Duration.ofSeconds(30));

        private static final CachedAccessToken INITIAL_PENDING_REFRESH = new CachedAccessToken(null, Instant.MIN, Instant.MIN);

        final String token;
        private final Instant invalidFrom;
        private final Instant refreshDueFrom;

        private CachedAccessToken(String token, Instant expiresAt) {
            this(token, expiresAt.minus(SAFETY_MARGIN_FOR_EXPIRY), expiresAt.minus(REFRESH_MARGIN));
        }

        private CachedAccessToken(String token, Instant invalidFrom, Instant refreshDueFrom) {
            this.token = token;
            this.invalidFrom = invalidFrom;
            this.refreshDueFrom = refreshDueFrom;
        }

        boolean isValidAt(Instant instant) {
            return instant.isBefore(invalidFrom);
        }

        boolean isRefreshDueAt(Instant instant) {
            return !instant.isBefore(refreshDueFrom);
        }
    }

}
