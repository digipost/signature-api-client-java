package no.digipost.signature.client.core.internal.http;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import no.digipost.signature.client.core.exceptions.AccessTokenException;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.core.internal.configuration.ApacheHttpClientBuilderConfigurer;
import no.digipost.signature.client.core.internal.configuration.Configurer;
import no.digipost.signature.client.security.KeyStoreConfig;
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
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static no.digipost.signature.client.core.internal.http.StatusCode.Family.SUCCESSFUL;
import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;

/**
 * Acquires and caches OAuth 2.0 access tokens using the <em>client credentials</em> grant, where
 * the client authenticates to the token endpoint by presenting its client certificate during the
 * TLS handshake.
 *
 * <p>Tokens are cached in memory and refreshed lazily: a token is considered stale
 * {@value #REFRESH_MARGIN_SECONDS} seconds before its actual expiry, and the next call to
 * {@link #getToken()} after that point acquires a new one. There is no background refresh thread.
 */
public class MutualTlsTokenProvider {

    private static final Logger LOG = Logger.getLogger(MutualTlsTokenProvider.class.getName());

    static final long REFRESH_MARGIN_SECONDS = 30;

    private static final Duration REFRESH_MARGIN = Duration.ofSeconds(REFRESH_MARGIN_SECONDS);

    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String EXPIRES_IN_FIELD = "expires_in";

    private static final JsonFactory JSON = new JsonFactory();

    /**
     * Error responses from a token endpoint are included in exception messages to aid debugging,
     * but truncated so that a misconfigured endpoint returning e.g. an HTML error page does not
     * produce an unreadable exception.
     */
    private static final int MAX_REPORTED_ERROR_BODY_LENGTH = 512;


    private final URI tokenEndpointUri;
    private final List<NameValuePair> tokenRequestParameters;
    private final HttpClient tokenClient;
    private final Clock clock;

    private final Object refreshLock = new Object();
    private volatile CachedAccessToken cachedToken;


    /**
     * Create a token provider with an HTTP client which authenticates using the client certificate
     * of the given {@link KeyStoreConfig}.
     *
     * <p>The token endpoint belongs to Digipost's own identity provider, mIdP, which is a separate
     * service from the Posten signering API. The same client certificate can be used to acquire
     * tokens for several Digipost services, but a token is issued for one
     * {@link AccessTokenRequest#scope scope} at a time, so a token acquired for Posten signering
     * cannot also be used against another service.
     *
     * <p>Being a separate service, its certificate is validated the ordinary way: against the JVM's
     * default trust store, with ordinary hostname verification. It deliberately does <em>not</em>
     * reuse the trust configuration used for the Posten signering API itself, which instead requires
     * the server certificate to identify Posten Bring AS, and skips hostname verification on that
     * basis.
     *
     * @param accessTokenRequest            the resolved parameters to request an access token with
     * @param keyStoreConfig                the client certificate to authenticate with
     * @param commonHttpClientConfiguration configuration shared with the API clients, such as
     *                                      User-Agent and proxy settings
     * @param clock                         the clock used to determine token expiry
     */
    public static MutualTlsTokenProvider create(
            AccessTokenRequest accessTokenRequest,
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

        return new MutualTlsTokenProvider(accessTokenRequest, tokenClientBuilder.build(), clock);
    }

    /**
     * @param accessTokenRequest the resolved parameters to request an access token with
     * @param tokenClient        the HTTP client used to call the token endpoint. It is the caller's
     *                           responsibility that this client presents the appropriate client
     *                           certificate, cf. {@link #create(AccessTokenRequest, KeyStoreConfig, Configurer, Clock)}.
     * @param clock              the clock used to determine token expiry
     */
    public MutualTlsTokenProvider(AccessTokenRequest accessTokenRequest, HttpClient tokenClient, Clock clock) {
        this.tokenEndpointUri = requireNonNull(accessTokenRequest, "access token request").tokenEndpoint;
        this.tokenClient = requireNonNull(tokenClient, "token endpoint HTTP client");
        this.clock = requireNonNull(clock, "clock");
        this.tokenRequestParameters = clientCredentialsParameters(accessTokenRequest);
    }


    /**
     * Get a valid access token, acquiring a new one from the token endpoint if the currently
     * cached token is absent or about to expire.
     *
     * @return the access token, to be used as an opaque bearer token
     */
    public String getToken() {
        CachedAccessToken current = cachedToken;
        if (current != null && current.isValidAt(Instant.now(clock))) {
            return current.token;
        }
        synchronized (refreshLock) {
            // Another thread may have refreshed the token while we waited for the lock.
            current = cachedToken;
            if (current != null && current.isValidAt(Instant.now(clock))) {
                return current.token;
            }
            CachedAccessToken refreshed = acquireToken();
            cachedToken = refreshed;
            return refreshed.token;
        }
    }


    /**
     * Discard the cached access token, so that the next call to {@link #getToken()} acquires a new
     * one. Used when the API has rejected the given token, which can happen before it is considered
     * stale here, e.g. if it was revoked, or if this host's clock runs ahead of the token endpoint's.
     * <p>
     * The token is only discarded if it is still the one currently cached, so that a token another
     * thread has just acquired is not thrown away.
     *
     * @param rejectedToken the access token which was rejected
     */
    public void invalidate(String rejectedToken) {
        synchronized (refreshLock) {
            CachedAccessToken current = cachedToken;
            if (current != null && current.token.equals(rejectedToken)) {
                cachedToken = null;
                LOG.fine(() -> "Discarded the cached access token from " + tokenEndpointUri + ", as it was rejected");
            }
        }
    }

    private CachedAccessToken acquireToken() {
        ClassicHttpRequest request = ClassicRequestBuilder
                .post(tokenEndpointUri)
                .addHeader(ACCEPT, APPLICATION_JSON.getMimeType())
                .setEntity(new UrlEncodedFormEntity(tokenRequestParameters, UTF_8))
                .build();

        try {
            return tokenClient.execute(request, this::handleTokenResponse);
        } catch (IOException e) {
            throw new HttpIOException(request, "Unable to acquire an access token from " + tokenEndpointUri, e);
        }
    }

    private CachedAccessToken handleTokenResponse(ClassicHttpResponse response) throws IOException, ParseException {
        String body = readBody(response);

        StatusCode status = StatusCode.from(response.getCode());
        if (!status.is(SUCCESSFUL)) {
            throw new AccessTokenException(
                    "Got " + status + " from the token endpoint " + tokenEndpointUri +
                    ", expected a successful response. The response body was: " + truncate(body));
        }

        TokenResponse tokenResponse = readTokenResponse(body);

        Instant expiry = Instant.now(clock).plusSeconds(tokenResponse.expiresInSeconds);
        Instant staleAt = expiry.minus(REFRESH_MARGIN);
        if (!staleAt.isAfter(Instant.now(clock))) {
            LOG.warning("The access token acquired from " + tokenEndpointUri + " expires at " + expiry + ", which is " +
                    "already within the " + REFRESH_MARGIN_SECONDS + " second refresh margin. A new token will be " +
                    "acquired for every request, which may put considerable load on the token endpoint.");
        }

        LOG.fine(() -> "Acquired a new access token from " + tokenEndpointUri + ", expiring at " + expiry);
        return new CachedAccessToken(tokenResponse.accessToken, staleAt);
    }

    /**
     * Read the {@code access_token} and {@code expires_in} fields of the token endpoint's response.
     * Both are required: without a lifetime there is no basis for caching the token, and a response
     * lacking it is treated as one this client does not understand rather than something to guess
     * around.
     * <p>
     * Any other fields, including nested ones, are skipped.
     */
    private TokenResponse readTokenResponse(String responseBody) throws IOException {
        String accessToken = null;
        Long expiresInSeconds = null;

        try (JsonParser json = JSON.createParser(responseBody)) {
            if (json.nextToken() != JsonToken.START_OBJECT) {
                throw new AccessTokenException(
                        "Expected the response from the token endpoint " + tokenEndpointUri + " to be a JSON object, " +
                        "but it was: " + truncate(responseBody));
            }
            while (json.nextToken() == JsonToken.FIELD_NAME) {
                // Using getCurrentName() and JsonProcessingException from older versions
                // in case consumers pin their own Jackson version and get a NoSuchMethodError at runtime
                String field = json.getCurrentName();
                JsonToken value = json.nextToken();
                if (ACCESS_TOKEN_FIELD.equals(field) && value == JsonToken.VALUE_STRING) {
                    accessToken = json.getText();
                } else if (EXPIRES_IN_FIELD.equals(field) && value == JsonToken.VALUE_NUMBER_INT) {
                    expiresInSeconds = json.getLongValue();
                } else {
                    // No-op for scalars, and skips past the contents of nested objects and arrays.
                    json.skipChildren();
                }
            }
        } catch (JsonProcessingException e) {
            throw new AccessTokenException(
                    "Could not parse the response from the token endpoint " + tokenEndpointUri + " as JSON, because " +
                    e.getClass().getSimpleName() + ": '" + e.getOriginalMessage() + "'", e);
        }

        if (accessToken == null || accessToken.isEmpty()) {
            throw new AccessTokenException(
                    "The response from the token endpoint " + tokenEndpointUri + " did not contain a non-empty " +
                    "'" + ACCESS_TOKEN_FIELD + "' string field.");
        }
        if (expiresInSeconds == null) {
            throw new AccessTokenException(
                    "The response from the token endpoint " + tokenEndpointUri + " did not contain an " +
                    "'" + EXPIRES_IN_FIELD + "' integer field, so it is not known how long the access token is valid.");
        }

        // The return expiry value of this method is passed to instant.plusSeconds(expiresInSeconds)
        // which would overflow on a malformed value close to Long.MAX_VALUE, which would return
        // ArithmeticException/DateTimeException rather than AccessTokenException. Not likely to
        // happen, and jwt expiry would never be this long/large, but checking that it's not malformed
        // Also handles "negative expires_in → new token every request"
        long theoreticalMaxExpiry = Duration.ofDays(365).getSeconds();
        if (expiresInSeconds <= 0 || expiresInSeconds > theoreticalMaxExpiry) {
            throw new AccessTokenException(
                    "The response from the token endpoint " + tokenEndpointUri + " stated a lifetime of " +
                    expiresInSeconds + " seconds for the access token, which is not a usable value. Expected a " +
                    "positive number of seconds, and at most " + theoreticalMaxExpiry + ".");
        }

        return new TokenResponse(accessToken, expiresInSeconds);
    }

    private static String readBody(ClassicHttpResponse response) throws IOException, ParseException {
        HttpEntity entity = response.getEntity();
        return entity == null ? "" : EntityUtils.toString(entity, UTF_8);
    }

    private static String truncate(String body) {
        if (body.isEmpty()) {
            return "(empty)";
        }
        return body.length() <= MAX_REPORTED_ERROR_BODY_LENGTH
            ? body
            : body.substring(0, MAX_REPORTED_ERROR_BODY_LENGTH) + "... (truncated)";
    }

    private static List<NameValuePair> clientCredentialsParameters(AccessTokenRequest request) {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        parameters.add(new BasicNameValuePair("client_id", request.clientId));
        parameters.add(new BasicNameValuePair("scope", request.scope));
        parameters.add(new BasicNameValuePair("resource", request.resource));
        return unmodifiableList(parameters);
    }

    /**
     * Build an {@link SSLContext} which presents the client certificate of the given key store,
     * and which validates the server using the JVM's default trust store.
     */
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
     * The fields of interest from a token endpoint response.
     */
    private static final class TokenResponse {

        final String accessToken;
        final long expiresInSeconds;

        TokenResponse(String accessToken, long expiresInSeconds) {
            this.accessToken = accessToken;
            this.expiresInSeconds = expiresInSeconds;
        }
    }


    /**
     * An acquired token together with the point in time where it should be replaced. Kept as a
     * single immutable value in one field, so that a token and its expiry can never be observed
     * out of sync by a thread reading the cache without holding the refresh lock.
     */
    private static final class CachedAccessToken {

        final String token;
        private final Instant staleAt;

        CachedAccessToken(String token, Instant staleAt) {
            this.token = token;
            this.staleAt = staleAt;
        }

        boolean isValidAt(Instant time) {
            return time.isBefore(staleAt);
        }
    }

}
