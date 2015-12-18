package no.digipost.signature.client.portal;

import no.digipost.signering.schema.v1.portal_signature_job.XMLSignatureStatus;

public enum SignatureStatus {

    WAITING,
    SIGNED;

    public static SignatureStatus fromXmlType(XMLSignatureStatus xmlSignatureStatus) {
        switch (xmlSignatureStatus) {
            case WAITING:
                return WAITING;
            case SIGNED:
                return SIGNED;
            default:
                throw new IllegalArgumentException("Unexpected status: " + xmlSignatureStatus);
        }
    }

}
