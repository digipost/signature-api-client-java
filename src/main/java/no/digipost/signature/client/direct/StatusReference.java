/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client.direct;

import javax.ws.rs.core.UriBuilder;

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
     * @see #ofUrl(String)
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
    public static StatusUrlContruction ofUrl(final String statusUrl) {
        return new StatusUrlContruction() {
            @Override
            public StatusReference withStatusQueryToken(String token) {
                return new StatusReference(statusUrl, token);
            }
        };
    }

    public static final String STATUS_QUERY_TOKEN_PARAM_NAME = "status_query_token";

    private final String statusUrl;
    private final String statusQueryToken;

    private StatusReference(String statusUrl, String statusQueryToken) {
        this.statusUrl = statusUrl;
        this.statusQueryToken = Objects.requireNonNull(statusQueryToken, STATUS_QUERY_TOKEN_PARAM_NAME);
    }

    public String getStatusUrl() {
        return UriBuilder.fromUri(statusUrl).queryParam(STATUS_QUERY_TOKEN_PARAM_NAME, statusQueryToken).build().toString();
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
