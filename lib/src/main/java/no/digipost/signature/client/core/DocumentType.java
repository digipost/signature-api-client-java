package no.digipost.signature.client.core;

public enum DocumentType {

    PDF("application/pdf", "pdf"),
    TXT("text/plain", "txt");

    private final String mediaType;
    private final String fileExtension;

    DocumentType(String mediaType, String fileExtension) {
        this.mediaType = mediaType;
        this.fileExtension = fileExtension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

}
