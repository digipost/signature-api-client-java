package no.digipost.signature.client.direct;

import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
/**
 * A {@code StatusReference} is constructed from the url acquired from
 * {@link DirectJobResponse#getStatusUrl()}, and a token provided as a
 * {@link StatusReference#STATUS_QUERY_TOKEN_PARAM_NAME query parameter} which is
 * added to the {@link ExitUrls exit URL} the signer is redirected to when
 * the signing ceremony is completed/aborted/failed. The token needs to
 * be consumed by the system the user is redirected to, and consequently provided to
 * {@link StatusUrlContruction#withStatusQueryToken(String)} to be able to construct a valid
 * complete {@code StatusReference} which is passed to {@link DirectClient#getStatus(StatusReference)}
 */
public class StatusReference {

    /**
     * Start constructing a new {@link StatusReference}.
     *
     * @param response the {@link DirectJobResponse}
     * @see #ofUrl(URI)
     */
    public static StatusUrlContruction of(DirectJobResponse response) {
        return ofUrl(response.getStatusUrl());
    }

    /**
     * Start constructing a new {@link StatusReference}.
     *
     * @param statusUrl the status url for the job
     * @return partially constructed {@link StatusReference} which
     *         must be completed with a status query token using
     *         {@link StatusUrlContruction#withStatusQueryToken(String) .withStatusQueryToken(token)}
     */
    public static StatusUrlContruction ofUrl(final URI statusUrl) {
        return new StatusUrlContruction() {
            @Override
            public StatusReference withStatusQueryToken(String token) {
                return new StatusReference(statusUrl, token);
            }
        };
    }

    public static final String STATUS_QUERY_TOKEN_PARAM_NAME = "status_query_token";

    private final URI statusUrl;
    private final String statusQueryToken;

    private StatusReference(URI statusUrl, String statusQueryToken) {
        this.statusUrl = statusUrl;
        this.statusQueryToken = Objects.requireNonNull(statusQueryToken, STATUS_QUERY_TOKEN_PARAM_NAME);
    }

    public URI getStatusUrl() {
        try {
            return new URIBuilder(statusUrl).addParameter(STATUS_QUERY_TOKEN_PARAM_NAME, statusQueryToken).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract class StatusUrlContruction {
        private StatusUrlContruction() {}

        /**
         * Create a complete {@link StatusReference} which can be passed to
         * {@link DirectClient#getStatus(StatusReference)}.
         *
         * @param token the status query token.
         * @return the {@code StatusReference}
         */
        public abstract StatusReference withStatusQueryToken(String token);
    }
}
