package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignatureJobStatus;

public enum DirectJobStatus {

    /**
     * At least one signer has not yet performed any action to the document.
     * For details about the state, see the {@link SignerStatus status} of each signer.
     *
     * @see XMLDirectSignatureJobStatus#IN_PROGRESS
     */
    IN_PROGRESS,

    /**
     * All signers have successfully signed the document.
     *
     * @see XMLDirectSignatureJobStatus#COMPLETED_SUCCESSFULLY
     */
    COMPLETED_SUCCESSFULLY,

    /**
     * All signers have performed an action to the document, but at least one have a non successful status (e.g. rejected, expired or failed).
     *
     * @see XMLDirectSignatureJobStatus#FAILED
     */
    FAILED,

    /**
     * There has not been any changes since the last received status change.
     */
    NO_CHANGES;

    public static DirectJobStatus fromXmlType(XMLDirectSignatureJobStatus xmlJobStatus) {
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
