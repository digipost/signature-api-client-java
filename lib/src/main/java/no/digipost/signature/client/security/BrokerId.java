package no.digipost.signature.client.security;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.ConfigurationException;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * The brokerId tied to the JWT client you've configured. It is used as part of the {@code scope} access
 * tokens are requested for. It is issued together with the {@link JwtAuthConfig#clientId client id},
 * this and there is exactly one broker id for a given client id in the signature-api specifically.
 *
 * <p>Note that this is <em>not</em> an organization number, and not an id used anywhere else in this
 * library. If you only act on behalf of your own organization, it is simply another id for it.
 *
 * <p>The broker id never changes for a client, so access tokens are always acquired as the same
 * organization. Which {@link Sender sender} a signature job is created for is separate from this,
 * and a broker acting on behalf of several organizations can set it per job with
 * {@link no.digipost.signature.client.portal.PortalJob.Builder#withSender(Sender) PortalJob.Builder.withSender(..)}
 * or {@link no.digipost.signature.client.direct.DirectJob.Builder#withSender(Sender) DirectJob.Builder.withSender(..)}.
 *
 * @see JwtAuthConfig
 */
public final class BrokerId {

    private final String id;

    /**
     * @param id the broker id for your client in signature-api
     */
    public static BrokerId of(String id) {
        requireNonNull(id, "broker id");
        if (id.trim().isEmpty()) {
            throw new ConfigurationException("The broker id must not be blank");
        }
        return new BrokerId(id);
    }

    private BrokerId(String id) {
        this.id = id;
    }

    /**
     * The broker id as the identity provider knows it.
     */
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
