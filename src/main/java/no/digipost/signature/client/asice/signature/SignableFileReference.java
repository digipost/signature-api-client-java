package no.digipost.signature.client.asice.signature;

public interface SignableFileReference {

    String getFileName();

    byte[] getSha256();

    String getMediaType();

}
