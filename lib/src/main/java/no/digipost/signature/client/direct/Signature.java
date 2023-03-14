package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.XAdESReference;

import java.time.Instant;
import java.util.function.Predicate;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;


public class Signature {

    private final String signer;

    private final SignerStatus status;
    private final Instant statusDateTime;

    private final XAdESReference xAdESReference;

    public Signature(String signer, SignerStatus status, Instant statusDateTime, XAdESReference xAdESReference) {
        this.signer = signer;
        this.status = status;
        this.statusDateTime = statusDateTime;
        this.xAdESReference = xAdESReference;
    }

    public String getSigner() {
        return signer;
    }

    public boolean isFrom(String personalIdentificationNumber) {
        return this.signer.equals(personalIdentificationNumber);
    }

    public boolean is(SignerStatus status) {
        return this.status == status;
    }

    public SignerStatus getStatus() {
        return status;
    }

    /**
     * @return Point in time when the action (document was signed, signature job expired, etc.) leading to the
     * current {@link Signature#status} happened.
     */
    public Instant getStatusDateTime() {
        return statusDateTime;
    }

    public XAdESReference getxAdESUrl() {
        return xAdESReference;
    }

    @Override
    public String toString() {
        return "Signature from " + mask(signer) + " with status '" + status + "' since " + statusDateTime + "" +
                (xAdESReference != null ? ". XAdES available at " + xAdESReference.getxAdESUrl() : "");
    }

    static Predicate<Signature> signatureFrom(final String signer) {
        return signature -> signature.isFrom(signer);
    }


}
