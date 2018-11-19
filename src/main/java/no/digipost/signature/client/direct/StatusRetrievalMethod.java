package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLStatusRetrievalMethod;
import no.digipost.signature.client.core.internal.MarshallableEnum;

public enum StatusRetrievalMethod implements MarshallableEnum<XMLStatusRetrievalMethod> {

    WAIT_FOR_CALLBACK(XMLStatusRetrievalMethod.WAIT_FOR_CALLBACK),
    POLLING(XMLStatusRetrievalMethod.POLLING);

    private final XMLStatusRetrievalMethod xmlValue;

    StatusRetrievalMethod(XMLStatusRetrievalMethod xmlValue) {
        this.xmlValue = xmlValue;
    }

    @Override
    public XMLStatusRetrievalMethod getXmlEnumValue() {
        return xmlValue;
    }
}
