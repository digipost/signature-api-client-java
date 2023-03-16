package no.digipost.signature.client.core.internal.xml;

import no.digipost.signature.jaxb.JaxbMarshaller;

import java.io.InputStream;
import java.io.OutputStream;

public final class Marshalling {

    public static void marshal(Object object, OutputStream entityStream) {
        JaxbMarshaller.ForRequestsOfAllApis.singleton().marshal(object, entityStream);
    }

    public static <T> T unmarshal(InputStream entityStream, Class<T> type) {
        return JaxbMarshaller.ForResponsesOfAllApis.singleton().unmarshal(entityStream, type);
    }

    private Marshalling() { }
}
