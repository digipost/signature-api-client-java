package no.digipost.signature.client.portal;

import no.digipost.signature.client.core.internal.Cancellable;

public class PortalJobResponse implements Cancellable {

    private final long signatureJobId;
    private final String reference;
    private final CancellationUrl cancellationUrl;

    public PortalJobResponse(long signatureJobId, String reference, CancellationUrl cancellationUrl) {
        this.signatureJobId = signatureJobId;
        this.reference = reference;
        this.cancellationUrl = cancellationUrl;
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

    @Override
    public CancellationUrl getCancellationUrl() {
        return cancellationUrl;
    }
}
