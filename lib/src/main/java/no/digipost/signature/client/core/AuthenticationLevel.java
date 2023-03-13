package no.digipost.signature.client.core;

import no.digipost.signature.api.xml.XMLAuthenticationLevel;
import no.digipost.signature.client.core.internal.MarshallableEnum;

public enum AuthenticationLevel implements MarshallableEnum<XMLAuthenticationLevel> {

    THREE(XMLAuthenticationLevel.THREE),
    FOUR(XMLAuthenticationLevel.FOUR);

    private final XMLAuthenticationLevel xmlEnumValue;

    @Override
    public XMLAuthenticationLevel getXmlEnumValue() {
        return xmlEnumValue;
    }

    AuthenticationLevel(XMLAuthenticationLevel xmlEnumValue) {
        this.xmlEnumValue = xmlEnumValue;
    }

}
