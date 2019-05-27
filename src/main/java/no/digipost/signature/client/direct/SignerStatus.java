package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignerStatusValue;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SignerStatus {

    private static final ConcurrentMap<XMLDirectSignerStatusValue, SignerStatus> KNOWN = new ConcurrentHashMap<>();

    /**
     * The signer has rejected to sign the document.
     */
    public static final SignerStatus REJECTED = SignerStatus.of(XMLDirectSignerStatusValue.REJECTED);

    /**
     * The signer has not made a decision to either sign or reject the document within the
     * specified time limit.
     */
    public static final SignerStatus EXPIRED = SignerStatus.of(XMLDirectSignerStatusValue.EXPIRED);

    /**
     * The signer has yet to review the document and decide if she/he wants to sign or
     * reject it.
     */
    public static final SignerStatus WAITING = SignerStatus.of(XMLDirectSignerStatusValue.WAITING);

    /**
     * The signer has successfully signed the document.
     */
    public static final SignerStatus SIGNED = SignerStatus.of(XMLDirectSignerStatusValue.SIGNED);

    /**
     * An unexpected error occured during the signing ceremony.
     */
    public static final SignerStatus FAILED = SignerStatus.of(XMLDirectSignerStatusValue.FAILED);

    /**
     * The job has reached a state where the status of this signature is not applicable.
     * This includes the case where a signer rejects to sign, and thus ending the job in a
     * {@link DirectJobStatus#FAILED} state. Any remaining (previously {@link #WAITING})
     * signatures are marked as {@link #NOT_APPLICABLE}.
     */
    public static final SignerStatus NOT_APPLICABLE = SignerStatus.of(XMLDirectSignerStatusValue.NOT_APPLICABLE);

    /**
     * Indicates that the service was unable to retrieve the signer's name.
     * <p>
     * This happens when the signer's name is permanently unavailable in the lookup service,
     * creating and signing a new signature job with the same signer will yield the same result.
     * <p>
     * Only applicable for {@link no.digipost.signature.client.core.SignatureType#AUTHENTICATED_SIGNATURE authenticated signatures}
     * where the sender requires signed documents to contain {@link IdentifierInSignedDocuments#NAME name}
     * as {@link DirectJob.Builder#withIdentifierInSignedDocuments(IdentifierInSignedDocuments) the signer's identifier}.
     */
    public static final SignerStatus SIGNERS_NAME_NOT_AVAILABLE = SignerStatus.of(XMLDirectSignerStatusValue.SIGNERS_NAME_NOT_AVAILABLE);


    static final SignerStatus of(XMLDirectSignerStatusValue status) {
        Objects.requireNonNull(status, XMLDirectSignerStatusValue.class.getSimpleName());
        return KNOWN.computeIfAbsent(status, key -> new SignerStatus(key.asString()));
    }

    private final String identifier;


    private SignerStatus(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the String identifier for this status.
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SignerStatus && Objects.equals(this.identifier, ((SignerStatus) o).identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }

}
