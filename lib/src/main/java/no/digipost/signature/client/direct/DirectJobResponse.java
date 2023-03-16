package no.digipost.signature.client.direct;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;

public class DirectJobResponse {
    private final long signatureJobId;
    private final String reference;
    private final List<DirectSignerResponse> signers;
    private final URI statusUrl;

    public DirectJobResponse(long signatureJobId, String reference, URI statusUrl, List<DirectSignerResponse> signers) {
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

    /**
     * Get the signer with the given identifier for this job.
     *
     * @param identifier the string identifying the signer, either a personal identification number,
     *                   or a custom identifier
     * @return the signer with the given identifier
     * @throws NoSuchElementException if the signer was not found.
     */
    public DirectSignerResponse getSignerIdentifiedBy(String identifier) {
        return findSignerIdentifiedBy(identifier).orElseThrow(() -> new NoSuchElementException(
                "signer with identifier " + mask(identifier) + " in job " + signatureJobId +
                (reference != null ? " (reference: " + reference + ")" : "") + "."));
    }

    /**
     * Try to find the signer with the given identifier for this job. If you expect the signer to exist,
     * consider using {@link #getSignerIdentifiedBy(String)} instead.
     *
     * @param identifier the string identifying the signer, either a personal identification number,
     *                   or a custom identifier
     * @return the found {@link DirectSignerResponse signer}, or {@link Optional#empty() empty}.
     */
    public Optional<DirectSignerResponse> findSignerIdentifiedBy(String identifier) {
        return getSigners().stream().filter(signer -> signer.hasIdentifier(identifier)).findFirst();
    }

    public URI getStatusUrl() {
        return statusUrl;
    }
}
