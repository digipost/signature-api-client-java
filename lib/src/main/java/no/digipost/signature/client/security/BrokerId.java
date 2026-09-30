package no.digipost.signature.client.security;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.ConfigurationException;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * The broker id issued together with your {@link JwtAuthConfig#clientId client id}. Both can be found in
 * Digipost's self-service portal for managing certificates and clients.
 * <p>
 * BrokerId is not an organization number. If you only act on behalf of your own organization, it is simply another id
 * for it. A broker acting on behalf of several organizations sets the {@link Sender sender} per job with
 * {@code PortalJob.Builder.withSender(..)} or {@code DirectJob.Builder.withSender(..)}.
 */
public final class BrokerId {

    private final String id;

    /**
     * @throws ConfigurationException if the id is empty or contains whitespace
     */
    public static BrokerId of(String id) {
        requireNonNull(id, "broker id");
        if (id.isEmpty() || id.chars().anyMatch(Character::isWhitespace)) {
            throw new ConfigurationException("The broker id must not be empty or contain whitespace, but was '" + id + "'");
        }
        return new BrokerId(id);
    }

    private BrokerId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BrokerId && Objects.equals(this.id, ((BrokerId) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "broker " + id;
    }

}
