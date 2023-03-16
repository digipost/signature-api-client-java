package no.digipost.signature.client.asice;

import no.digipost.signature.client.asice.signature.SignableFileReference;

import static no.digipost.signature.client.core.internal.security.DigestUtils.digest;
import static no.digipost.signature.client.core.internal.security.DigestUtils.Algorithm.SHA256;

public interface ASiCEAttachable extends SignableFileReference {

    public static final String XML_MEDIATYPE = "application/xml";

    @Override
    String getFileName();

    byte[] getContent();

    @Override
    String getMediaType();

    @Override
    default byte[] getSha256() {
        return digest(SHA256, getContent());
    }
}
