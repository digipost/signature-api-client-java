package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static no.digipost.signature.client.core.internal.xml.Marshalling.marshal;

public abstract class ManifestCreator<JOB extends SignatureJob> {

    public Manifest createManifest(JOB job, Sender sender) {
        Object xmlManifest = buildXmlManifest(job, sender);

        try (ByteArrayOutputStream manifestStream = new ByteArrayOutputStream()) {
            marshal(xmlManifest, manifestStream);
            return new Manifest(manifestStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    abstract Object buildXmlManifest(JOB job, Sender sender);


}
