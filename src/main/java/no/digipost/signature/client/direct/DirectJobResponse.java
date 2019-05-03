package no.digipost.signature.client.direct;

import java.util.List;

public class DirectJobResponse {
    private final long signatureJobId;
    private final String reference;
    private final List<DirectSignerResponse> signers;
    private final String statusUrl;

    public DirectJobResponse(long signatureJobId, String reference, String statusUrl, List<DirectSignerResponse> signers) {
        this.signatureJobId = signatureJobId;
        this.reference = reference;
        this.signers = signers;
        this.statusUrl = statusUrl;
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    /**
     * @return the signature job's custom reference as specified upon
     * {@link DirectJob.Builder#withReference(String) creation}. May be {@code null}.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Gets the single signer for this job.
     * Convenience method for retrieving the signer for jobs with exactly one signer.
     *
     * @return the signer
     *
     * @throws IllegalStateException if there are multiple signers for this job
     * @see #getSigners()
     */
    public DirectSignerResponse getSingleSigner() {
        if (signers.size() != 1) {
            throw new IllegalStateException("Calls to this method should only be done when there are no more than one signer.");
        }
        return signers.get(0);
    }

    /**
     * Gets all the {@link DirectSignerResponse signers} for this job
     *
     * @return the signers
     */
    public List<DirectSignerResponse> getSigners() {
        return signers;
    }

    public String getStatusUrl() {
        return statusUrl;
    }
}
