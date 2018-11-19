package no.digipost.signature.client.portal;

import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.DeleteDocumentsUrl;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.internal.Cancellable;
import no.digipost.signature.client.core.internal.Confirmable;

import java.time.Instant;
import java.util.List;

import static no.digipost.signature.client.portal.PortalJobStatus.NO_CHANGES;
import static no.digipost.signature.client.portal.Signature.signatureFrom;

/**
 * Indicates a job which has got a new {@link PortalJobStatus status}
 * since the last time its status was queried.
 *
 * <h3>Confirmation</h3>
 *
 * When the client {@link Confirmable confirms} this, the job and its associated
 * resources will become unavailable through the Signature API.
 */
public class PortalJobStatusChanged implements Confirmable, Cancellable {


    /**
     * This instance indicates that there has been no status updates since the last poll request for
     * {@link PortalJobStatusChanged}. Its status is {@link PortalJobStatus#NO_CHANGES NO_CHANGES}.
     */
    static PortalJobStatusChanged noUpdatedStatus(Instant nextPermittedPollTime) {
        return new PortalJobStatusChanged(null, null, NO_CHANGES, null, null, null, null, null, nextPermittedPollTime) {
            @Override public long getSignatureJobId() {
                throw new IllegalStateException(
                        "There were " + this + ", and querying the job ID is a programming error. " +
                        "Use the method is(" + PortalJobStatus.class.getSimpleName() + "." + NO_CHANGES.name() + ") " +
                        "to check if there were any status change before attempting to get any further information.");
            }

            @Override public String toString() {
                return "no portal jobs with updated status";
            }
        };
    }

    private final Long signatureJobId;
    private final String reference;
    private final PortalJobStatus status;
    private final DeleteDocumentsUrl deleteDocumentsUrl;
    private final PAdESReference pAdESReference;
    private final ConfirmationReference confirmationReference;
    private final CancellationUrl cancellationUrl;
    private final List<Signature> signatures;
    private final Instant nextPermittedPollTime;

    PortalJobStatusChanged(Long signatureJobId, String reference, PortalJobStatus status, ConfirmationReference confirmationReference, CancellationUrl cancellationUrl, DeleteDocumentsUrl deleteDocumentsUrl, PAdESReference pAdESReference, List<Signature> signatures, Instant nextPermittedPollTime) {
        this.signatureJobId = signatureJobId;
        this.reference = reference;
        this.status = status;
        this.cancellationUrl = cancellationUrl;
        this.deleteDocumentsUrl = deleteDocumentsUrl;
        this.pAdESReference = pAdESReference;
        this.confirmationReference = confirmationReference;
        this.signatures = signatures;
        this.nextPermittedPollTime = nextPermittedPollTime;
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    /**
     * @return the signature job's custom reference as specified upon
     * {@link PortalJob.Builder#withReference(String) creation}. May be {@code null}.
     */
    public String getReference() {
        return reference;
    }

    public PortalJobStatus getStatus() {
        return status;
    }

    public boolean is(PortalJobStatus status) {
        return this.status == status;
    }

    public boolean isPAdESAvailable() {
        return pAdESReference != null;
    }

    public PAdESReference getpAdESUrl() {
        return pAdESReference;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    /**
     * Gets the signature from a given signer.
     *
     * @param signer an identifier referring to a signer of the job. It may be a personal identification number or
     *               contact information, depending of how the {@link PortalSigner signer} was initially created
     *               (using {@link PortalSigner#identifiedByPersonalIdentificationNumber(String, Notifications) personal identification number}<sup>1</sup>,
     *               {@link PortalSigner#identifiedByPersonalIdentificationNumber(String, NotificationsUsingLookup) personal identification number}<sup>2</sup>,
     *               {@link PortalSigner#identifiedByEmail(String) email address}, {@link PortalSigner#identifiedByMobileNumber(String) mobile number} or
     *               {@link PortalSigner#identifiedByEmailAndMobileNumber(String, String) both email address and mobile number}).
     *               <p>
     *               <sup>1</sup>: with contact information provided.<br>
     *               <sup>2</sup>: using contact information from a lookup service.
     *               </p>
     * @throws IllegalArgumentException if the job response doesn't contain a signature from this signer
     */
    public Signature getSignatureFrom(SignerIdentifier signer) {
        return signatures.stream()
                .filter(signatureFrom(signer))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find signature from this signer"));
    }

    public Instant getNextPermittedPollTime() {
        return nextPermittedPollTime;
    }

    @Override
    public ConfirmationReference getConfirmationReference() {
        return confirmationReference;
    }

    @Override
    public CancellationUrl getCancellationUrl() {
        return cancellationUrl;
    }

    public DeleteDocumentsUrl getDeleteDocumentsUrl() {
        return deleteDocumentsUrl;
    }

    @Override
    public String toString() {
        return "updated status for portal job with id " + signatureJobId + ": " + status;
    }
}
