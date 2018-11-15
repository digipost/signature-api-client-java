package no.digipost.signature.client.asice;

public interface ASiCEAttachable {
    String getFileName();

    byte[] getBytes();

    String getMimeType();
}
