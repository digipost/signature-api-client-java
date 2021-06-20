package no.digipost.signature.client.core;

import no.digipost.signature.client.asice.ASiCEAttachable;

public class Document implements ASiCEAttachable {

    private final String title;
    private final String mediaType;
    private final String fileName;
    private final byte[] document;

    public Document(String title, String mediaType, String fileName, byte[] document) {
        this.title = title;
        this.mediaType = mediaType;
        this.fileName = fileName;
        this.document = document;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public byte[] getBytes() {
        return document;
    }

}
