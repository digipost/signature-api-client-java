package no.digipost.signature.client.core.internal.xml;

import no.digipost.signature.jaxb.spring.SignatureJaxb2Marshaller;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;

public final class Marshalling {

    public static void marshal(Object object, OutputStream entityStream) {
        SignatureJaxb2Marshaller.ForRequestsOfAllApis.singleton().marshal(object, new StreamResult(entityStream));
    }

    public static Object unmarshal(InputStream entityStream) {
        return SignatureJaxb2Marshaller.ForResponsesOfAllApis.singleton().unmarshal(new StreamSource(entityStream));
    }

    private Marshalling() { }
}
