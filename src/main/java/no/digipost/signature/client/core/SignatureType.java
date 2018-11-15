package no.digipost.signature.client.core;

import no.digipost.signature.api.xml.XMLSignatureType;
import no.digipost.signature.client.core.internal.MarshallableEnum;

/**
 * Specifies which type of signature to facilitate for a signer.
 */
public enum SignatureType implements MarshallableEnum<XMLSignatureType> {

    AUTHENTICATED_SIGNATURE(XMLSignatureType.AUTHENTICATED_ELECTRONIC_SIGNATURE),
    ADVANCED_SIGNATURE(XMLSignatureType.ADVANCED_ELECTRONIC_SIGNATURE);

    private final XMLSignatureType xmlEnumValue;

    SignatureType(XMLSignatureType xmlEnumValue) {
        this.xmlEnumValue = xmlEnumValue;
    }

    @Override
    public XMLSignatureType getXmlEnumValue() {
        return xmlEnumValue;
    }

}
