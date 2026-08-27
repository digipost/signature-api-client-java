package no.digipost.signature.client.core.internal.http;

import java.net.URI;

import static java.util.Objects.requireNonNull;

/**
 * The fully resolved inputs for requesting an access token with the OAuth 2.0 <em>client
 * credentials</em> grant. Resolving these is the responsibility of
 * {@link no.digipost.signature.client.ClientConfiguration ClientConfiguration}, which knows both the
 * {@link no.digipost.signature.client.security.JwtAuthConfig JwtAuthConfig} and the
 * {@link no.digipost.signature.client.ServiceEnvironment ServiceEnvironment} they are derived from.
 *
 * <p>Exists as a value object with named fields rather than as loose parameters because
 * {@link #scope} and {@link #resource} are both strings which the token endpoint matches exactly.
 */
public final class AccessTokenRequest {

    /**
     * The endpoint to request the access token from.
     */
    public final URI tokenEndpoint;

    /**
     * Sent as the {@code client_id} parameter.
     */
    public final String clientId;

    /**
     * Sent as the {@code scope} parameter.
     */
    public final String scope;

    /**
     * Sent as the {@code resource} parameter.
     */
    public final String resource;

    public AccessTokenRequest(URI tokenEndpoint, String clientId, String scope, String resource) {
        this.tokenEndpoint = requireNonNull(tokenEndpoint, "token endpoint");
        this.clientId = requireNonNull(clientId, "client id");
        this.scope = requireNonNull(scope, "scope");
        this.resource = requireNonNull(resource, "resource");
    }

    @Override
    public String toString() {
        return "Access token request to " + tokenEndpoint + " for client '" + clientId + "', " +
                "scope '" + scope + "', resource '" + resource + "'";
    }

}
