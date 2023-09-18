package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.jaxb.JaxbMarshaller;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityTemplate;

import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;

public final class ApacheHttpMarshallingSupport {

    private final JaxbMarshaller marshaller;

    public ApacheHttpMarshallingSupport(JaxbMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public HttpEntity createEntity(Object jaxbObject) {
        return new EntityTemplate(-1, APPLICATION_XML, "UTF-8", out -> marshaller.marshal(jaxbObject, out));
    }

    public ContentBody createContentBody(Object jaxbObject) {
        return createContentBody("", jaxbObject);
    }

    public ContentBody createContentBody(String filename, Object jaxbObject) {
        return new IOCallbackContentBody(-1, APPLICATION_XML, filename, out -> marshaller.marshal(jaxbObject, out));
    }

}
