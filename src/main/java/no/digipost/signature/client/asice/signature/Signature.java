package no.digipost.signature.client.asice.signature;

import no.digipost.signature.client.asice.ASiCEAttachable;

public class Signature implements ASiCEAttachable {

    private final byte[] xmlBytes;

    public Signature(byte[] xmlBytes) {
        this.xmlBytes = xmlBytes;
    }

    @Override
    public String getFileName() {
        return "META-INF/signatures.xml";
    }

    @Override
    public byte[] getContent() {
        return xmlBytes;
    }

    @Override
    public String getMediaType() {
        return XML_MEDIATYPE;
    }

}
