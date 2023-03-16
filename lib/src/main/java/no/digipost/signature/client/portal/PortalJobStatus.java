package no.digipost.signature.client.portal;

import no.digipost.signature.api.xml.XMLPortalSignatureJobStatus;
import no.digipost.signature.client.core.internal.Confirmable;

public enum PortalJobStatus {


	/**
     * Indicates that there has been a change to the job, but that it has not been signed by all signers yet. For details about the state, see the {@link SignatureStatus status} of each signer.
     *
     * When the client {@link Confirmable confirms} a job with this status,
     * the job is removed from the queue and will not be returned upon subsequent polling,
     * until the status has changed again.
     */
    IN_PROGRESS,

    /**
     * Indicates that the signature job has completed successfully with signatures from all signers.
     *
     * When the client {@link Confirmable confirms} a job with this status,
     * the job and its associated resources will become unavailable through the Signature API.
     */
    COMPLETED_SUCCESSFULLY,

    /**
     * Indicates that the signature job failed. For details about the failure, see the {@link SignatureStatus status} of each signer.
     *
     * When the client {@link Confirmable confirms} a job with this status,
     * the job and its associated resources will become unavailable through the Signature API.
     */
    FAILED,

    /**
     * There has not been any changes since the last received status change.
     */
    NO_CHANGES;

    public static PortalJobStatus fromXmlType(XMLPortalSignatureJobStatus xmlJobStatus) {
        switch (xmlJobStatus) {
            case IN_PROGRESS:
                return IN_PROGRESS;
            case COMPLETED_SUCCESSFULLY:
                return COMPLETED_SUCCESSFULLY;
            case FAILED:
                return FAILED;
            default:
                throw new IllegalArgumentException("Unexpected status: " + xmlJobStatus);
        }
    }
}
