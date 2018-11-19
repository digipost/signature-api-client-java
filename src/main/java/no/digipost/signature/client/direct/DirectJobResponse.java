package no.digipost.signature.client.direct;

import no.digipost.signature.client.direct.RedirectUrls.RedirectUrl;

import java.util.List;

public class DirectJobResponse {
    private final long signatureJobId;
    private final String reference;
    private final RedirectUrls redirectUrls;
    private final String statusUrl;

    public DirectJobResponse(long signatureJobId, String reference, List<RedirectUrl> redirectUrls, String statusUrl) {
        this.signatureJobId = signatureJobId;
        this.reference = reference;
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
