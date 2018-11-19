package no.digipost.signature.client.core;

import no.digipost.signature.api.xml.XMLIdentifierInSignedDocuments;
import no.digipost.signature.client.core.internal.MarshallableEnum;

public enum IdentifierInSignedDocuments implements MarshallableEnum<XMLIdentifierInSignedDocuments> {

    PERSONAL_IDENTIFICATION_NUMBER_AND_NAME(XMLIdentifierInSignedDocuments.PERSONAL_IDENTIFICATION_NUMBER_AND_NAME),
    DATE_OF_BIRTH_AND_NAME(XMLIdentifierInSignedDocuments.DATE_OF_BIRTH_AND_NAME),
    NAME(XMLIdentifierInSignedDocuments.NAME),
    ;


    private final XMLIdentifierInSignedDocuments xmlEnumValue;

    IdentifierInSignedDocuments(XMLIdentifierInSignedDocuments xmlEnumValue) {
        this.xmlEnumValue = xmlEnumValue;
    }

    @Override
    public XMLIdentifierInSignedDocuments getXmlEnumValue() {
        return xmlEnumValue;
    }
}
