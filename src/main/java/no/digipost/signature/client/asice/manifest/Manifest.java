package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.client.asice.ASiCEAttachable;

public class Manifest implements ASiCEAttachable {

    private byte[] xmlBytes;

    public Manifest(final byte[] xmlBytes) {
        this.xmlBytes = xmlBytes;
    }

    @Override
    public String getFileName() {
        return "manifest.xml";
    }

    @Override
    public byte[] getBytes() {
        return xmlBytes;
    }

    @Override
    public String getMediaType() {
        return XML_MEDIATYPE;
    }

}
