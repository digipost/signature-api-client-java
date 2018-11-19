package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.exceptions.XmlValidationException;
import org.springframework.oxm.MarshallingFailureException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static no.digipost.signature.client.core.internal.xml.Marshalling.marshal;

public abstract class ManifestCreator<JOB extends SignatureJob> {

    public Manifest createManifest(JOB job, Sender sender) {
        Object xmlManifest = buildXmlManifest(job, sender);

        try (ByteArrayOutputStream manifestStream = new ByteArrayOutputStream()) {
            marshal(xmlManifest, manifestStream);
            return new Manifest(manifestStream.toByteArray());
        } catch (MarshallingFailureException e) {
            if (e.getMostSpecificCause() instanceof SAXParseException) {
                throw new XmlValidationException("Unable to validate generated Manifest XML. " +
                        "This typically happens if one or more values are not in accordance with the XSD. " +
                        "You may inspect the cause (by calling getCause()) to see which constraint has been violated.", (SAXParseException) e.getMostSpecificCause());
            }
            throw e;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    abstract Object buildXmlManifest(JOB job, Sender sender);


}
