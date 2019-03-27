package no.digipost.signature.client.asice;

import no.digipost.signature.client.asice.signature.SignableFileReference;

import static org.apache.commons.codec.digest.DigestUtils.sha256;

public interface ASiCEAttachable extends SignableFileReference {
    @Override
    String getFileName();

    byte[] getBytes();

    String getMimeType();

    @Override
    default byte[] getSha256() {
        return sha256(getBytes());
    }
}
