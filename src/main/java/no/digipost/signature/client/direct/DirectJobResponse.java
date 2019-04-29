package no.digipost.signature.client.direct;

import no.digipost.signature.client.direct.RedirectUrls.RedirectUrl;

import java.util.List;

public class DirectJobResponse {
    private final long signatureJobId;
    private final String reference;
    private final List<RedirectUrlRequest> redirectUrlRequests;
    private final RedirectUrls redirectUrls;
    private final String statusUrl;

    public DirectJobResponse(long signatureJobId, String reference, List<RedirectUrlRequest> redirectUrlRequests, List<RedirectUrl> redirectUrls, String statusUrl) {
        this.signatureJobId = signatureJobId;
        this.reference = reference;
        this.redirectUrlRequests = redirectUrlRequests;
        this.redirectUrls = new RedirectUrls(redirectUrls);
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
     * Gets the only signing URL for this job.
     * Convenience method for retrieving the signing URL for jobs with exactly one signer.
     * @throws IllegalStateException if there are multiple signers for this job
     * @see #getRedirectUrlRequests()
     */
    public RedirectUrlRequest getSingleRedirectUrlRequest() {
        if (redirectUrlRequests.size() != 1) {
            throw new IllegalStateException("Calls to this method should only be done when there are no more than one (1) signer.");
        }
        return redirectUrlRequests.get(0);
    }

    public List<RedirectUrlRequest> getRedirectUrlRequests() {
        return redirectUrlRequests;
    }


    /**
     * Gets the only redirect URL for this job.
     * Convenience method for retrieving the redirect URL for jobs with exactly one signer.
     * @throws IllegalStateException if there are multiple redirect URLs
     * @see #getRedirectUrls()
     */
    public String getSingleRedirectUrl() {
        return redirectUrls.getSingleRedirectUrl();
    }

    public RedirectUrls getRedirectUrls() {
        return redirectUrls;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

}
