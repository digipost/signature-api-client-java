package no.digipost.signature.client.security;

import no.digipost.signature.client.core.exceptions.ConfigurationException;

import static java.util.Objects.requireNonNull;

/**
 * The client id and broker id used to acquire access tokens for the API, found in
 * Digipost's self-service portal for managing certificates and clients.
 * <p>
 * See the <a href="https://signering-docs.readthedocs.io/en/latest/client-integration/create-client-configuration.html">docs</a>
 * for more information.
 *
 * @see no.digipost.signature.client.ClientConfiguration#builder(KeyStoreConfig, JwtAuthConfig)
 */
public final class JwtAuthConfig {

    /**
     * Sent as {@code client_id} to the token endpoint.
     */
    public final String clientId;

    /**
     * The broker the access token is issued for. Not the sender of a signature job.
     */
    public final BrokerId brokerId;

    /**
     * @param clientId the client id registered for your certificate
     * @param brokerId the broker the client id is registered for
     * @throws ConfigurationException if the client id is empty or contains whitespace
     */
    public static JwtAuthConfig forClient(String clientId, BrokerId brokerId) {
        requireNonNull(clientId, "client id");
        requireNonNull(brokerId, "broker id");
        if (clientId.isEmpty() || clientId.chars().anyMatch(Character::isWhitespace)) {
            throw new ConfigurationException("The client id must not be empty or contain whitespace, but was '" + clientId + "'");
        }
        return new JwtAuthConfig(clientId, brokerId);
    }

    private JwtAuthConfig(String clientId, BrokerId brokerId) {
        this.clientId = clientId;
        this.brokerId = brokerId;
    }

    @Override
    public String toString() {
        return "JWT authentication for client '" + clientId + "' as " + brokerId;
    }

}
