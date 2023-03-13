package no.digipost.signature.client.asice;

import no.digipost.signature.client.asice.signature.SignableFileReference;

import static org.apache.commons.codec.digest.DigestUtils.sha256;

public interface ASiCEAttachable extends SignableFileReference {

    public static final String XML_MEDIATYPE = "application/xml";

    @Override
    String getFileName();

    byte[] getContent();

    @Override
    String getMediaType();

    @Override
    default byte[] getSha256() {
        return sha256(getContent());
    }
}
