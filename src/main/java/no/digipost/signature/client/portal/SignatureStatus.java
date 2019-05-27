package no.digipost.signature.client.portal;

import no.digipost.signature.api.xml.XMLDirectSignerStatusValue;
import no.digipost.signature.api.xml.XMLPortalSignatureStatusValue;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SignatureStatus {

    private static final ConcurrentMap<XMLPortalSignatureStatusValue, SignatureStatus> KNOWN = new ConcurrentHashMap<>();

    /**
     * The signer has rejected to sign the document.
     */
    public static final SignatureStatus REJECTED = new SignatureStatus("REJECTED");

    /**
     * This signer has been cancelled by the sender, and will not be able to sign the document.
     */
    public static final SignatureStatus CANCELLED = new SignatureStatus("CANCELLED");

    /**
     * This signer is reserved from receiving documents electronically, and will not receive
     * the document for signing.
     */
    public static final SignatureStatus RESERVED = new SignatureStatus("RESERVED");

    /**
     * We were not able to locate any channels (email, SMS) for notifying the signer to sign the document.
     */
    public static final SignatureStatus CONTACT_INFORMATION_MISSING = new SignatureStatus("CONTACT_INFORMATION_MISSING");

    /**
     * The signer has not made a decision to either sign or reject the document within the
     * specified time limit,
     */
    public static final SignatureStatus EXPIRED = new SignatureStatus("EXPIRED");

    /**
     * The signer has yet to review the document and decide if she/he wants to sign or
     * reject it.
     */
    public static final SignatureStatus WAITING = new SignatureStatus("WAITING");

    /**
     * The signer has successfully signed the document.
     */
    public static final SignatureStatus SIGNED = new SignatureStatus("SIGNED");

    /**
     * The job has reached a state where the status of this signature is not applicable.
     * This includes the case where a signer rejects to sign, and thus ending the job in a
     * {@link PortalJobStatus#FAILED} state. Any remaining (previously {@link #WAITING})
     * signatures are marked as {@link #NOT_APPLICABLE}.
     */
    public static final SignatureStatus NOT_APPLICABLE = new SignatureStatus("NOT_APPLICABLE");

    /**
     * The signer entered the wrong security code too many times. Only applicable for
     * signers addressed by {@link PortalSigner#identifiedByEmail(String) e-mail address} or
     * {@link PortalSigner#identifiedByMobileNumber(String) mobile number}.
     */
    public static final SignatureStatus BLOCKED = new SignatureStatus("BLOCKED");

    /**
     * Indicates that the service was unable to retrieve the signer's name.
     * <p>
     * This happens when the signer's name is permanently unavailable in the lookup service,
     * creating and signing a new signature job with the same signer will yield the same result.
     * <p>
     * Only applicable for {@link no.digipost.signature.client.core.SignatureType#AUTHENTICATED_SIGNATURE authenticated signatures}
     * where the sender requires signed documents to contain {@link IdentifierInSignedDocuments#NAME name}
     * as {@link PortalJob.Builder#withIdentifierInSignedDocuments(IdentifierInSignedDocuments) the signer's identifier}.
     */
    public static final SignatureStatus SIGNERS_NAME_NOT_AVAILABLE = of(XMLPortalSignatureStatusValue.SIGNERS_NAME_NOT_AVAILABLE);


    static final SignatureStatus of(XMLPortalSignatureStatusValue status) {
        Objects.requireNonNull(status, XMLDirectSignerStatusValue.class.getSimpleName());
        return KNOWN.computeIfAbsent(status, key -> new SignatureStatus(key.asString()));
    }


    private final String identifier;

    private SignatureStatus(String identifier) {
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
        if (o instanceof SignatureStatus) {
            SignatureStatus that = (SignatureStatus) o;
            return Objects.equals(identifier, that.identifier);
        }
        return false;
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
