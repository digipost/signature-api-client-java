package no.digipost.signature.client.security;

import no.digipost.signature.client.core.exceptions.ConfigurationException;

import static java.util.Objects.requireNonNull;

/**
 * Configuration for authenticating with Posten signering using an OAuth 2.0
 * <em>client credentials</em> grant over a mutually authenticated TLS connection.
 *
 * The client authenticates to the token endpoint by presenting its client certificate
 * during the TLS handshake (together with a {@code client_id} form parameter), and the access
 * token returned is treated as an opaque bearer token which is sent as an
 * {@code Authorization: Bearer <token>} header on API requests.
 *
 * <p>The client certificate and private key are <em>not</em> part of this
 * configuration. The {@link KeyStoreConfig} already passed to
 * {@link no.digipost.signature.client.ClientConfiguration#builder(KeyStoreConfig) ClientConfiguration.builder(..)}
 * is used for the mTLS handshake against the token endpoint, and for signing document bundles.
 * The client certificate is <em>not</em> presented when connecting to the API itself:
 * those requests are authenticated with the acquired access token alone.
 *
 * <p>Neither is the token endpoint part of this configuration. It belongs to the
 * {@link no.digipost.signature.client.ServiceEnvironment ServiceEnvironment} the client is
 * configured with, which knows the endpoint for each of the predefined environments.
 *
 * @see no.digipost.signature.client.ClientConfiguration.Builder#jwtAuthentication(JwtAuthConfig)
 */
public final class JwtAuthConfig {

    /**
     * The client id identifying this integration to the token endpoint, sent as the
     * {@code client_id} parameter.
     */
    public final String clientId;

    /**
     * A JWT client is configured for a specific broker in signature-api and is used as part of the scope.
     * This is not the sender a signature job is created on behalf of.
     */
    public final BrokerId brokerId;

    /**
     * Configure JWT/mTLS authentication for the given client id and broker id, which are issued
     * together and belong to each other.
     *
     * @param clientId the client id registered for your certificate,
     *                 identifying this integration to the token endpoint
     * @param brokerId the {@link BrokerId broker} to acquire access tokens as
     */
    public static JwtAuthConfig forClient(String clientId, BrokerId brokerId) {
        requireNonNull(clientId, "client id");
        requireNonNull(brokerId, "broker id");
        if (clientId.trim().isEmpty()) {
            throw new ConfigurationException("The client id must not be blank");
        }
        return new JwtAuthConfig(clientId, brokerId);
    }

    private JwtAuthConfig(String clientId, BrokerId brokerId) {
        this.clientId = clientId;
        this.brokerId = brokerId;
    }

    @Override
    public String toString() {
        return "JWT/mTLS authentication for client '" + clientId + "' as " + brokerId;
    }

}
