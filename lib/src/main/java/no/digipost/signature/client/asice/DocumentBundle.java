package no.digipost.signature.client.asice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DocumentBundle {

    private final byte[] bytes;

    public DocumentBundle(byte[] bytes) {
        this.bytes = bytes;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public byte[] toByteArray() {
        return bytes;
    }
}
