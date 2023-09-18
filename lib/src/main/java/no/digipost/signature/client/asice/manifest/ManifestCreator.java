package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.api.xml.XMLManifest;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.jaxb.JaxbMarshaller;

public abstract class ManifestCreator<JOB extends SignatureJob> {

    private final JaxbMarshaller marshaller;

    protected ManifestCreator(JaxbMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public Manifest createManifest(JOB job) {
        Object xmlManifest = buildXmlManifest(job);
        return new Manifest(marshaller.marshalToBytes(xmlManifest));
    }

    abstract XMLManifest buildXmlManifest(JOB job);


}
