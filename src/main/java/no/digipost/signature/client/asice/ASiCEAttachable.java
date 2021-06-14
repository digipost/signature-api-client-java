package no.digipost.signature.client.asice;

import no.digipost.signature.client.asice.signature.SignableFileReference;

import static org.apache.commons.codec.digest.DigestUtils.sha256;

public interface ASiCEAttachable extends SignableFileReference {

    interface Type {
        static final Type XML = new Type() {
            @Override
            public String getMediaType() {
                return "application/xml";
            }

            @Override
            public String getFileExtension() {
                return "xml";
            }
        };

        String getMediaType();

        String getFileExtension();
    }


    @Override
    String getFileName();

    byte[] getBytes();

    ASiCEAttachable.Type getType();

    @Override
    default String getMimeType() {
        return getType().getMediaType();
    }

    @Override
    default byte[] getSha256() {
        return sha256(getBytes());
    }
}
