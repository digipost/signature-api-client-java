package no.digipost.signature.client.core;

import no.digipost.signature.client.asice.ASiCEAttachable;

public enum DocumentType implements ASiCEAttachable.Type {

    PDF("application/pdf", "pdf"),
    TXT("text/plain", "txt");

    private final String mediaType;
    private final String fileExtension;

    DocumentType(String mediaType, String fileExtension) {
        this.mediaType = mediaType;
        this.fileExtension = fileExtension;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

}
