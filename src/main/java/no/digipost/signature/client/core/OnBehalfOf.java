package no.digipost.signature.client.core;

import no.digipost.signature.api.xml.XMLSigningOnBehalfOf;
import no.digipost.signature.client.core.internal.MarshallableEnum;

/**
 * Specifies if the signer signs on behalf of itself or some other party
 */
public enum OnBehalfOf implements MarshallableEnum<XMLSigningOnBehalfOf> {

    SELF(XMLSigningOnBehalfOf.SELF),
    OTHER(XMLSigningOnBehalfOf.OTHER);

    private final XMLSigningOnBehalfOf xmlEnumValue;

    OnBehalfOf(XMLSigningOnBehalfOf xmlEnumValue) {
        this.xmlEnumValue = xmlEnumValue;
    }

    @Override
    public XMLSigningOnBehalfOf getXmlEnumValue() {
        return xmlEnumValue;
    }

}
