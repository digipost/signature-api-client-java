package no.digipost.signature.client.core.internal.http;

import java.net.URI;

import static java.util.Objects.requireNonNull;

/**
 * The parameters for requesting an access token with the <em>client credentials</em> grant.
 */
public final class AccessTokenRequest {

    public final URI tokenEndpoint;
    public final String clientId;
    public final String scope;
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
