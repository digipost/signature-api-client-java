package no.digipost.signature.client.core;

import no.digipost.signature.client.asice.ASiCEAttachable;

public abstract class Document implements ASiCEAttachable {

    private final String title;
    private final String fileName;
    private final byte[] document;
    private final FileType fileType;

    protected Document(String title, String fileName, FileType fileType, byte[] document) {
        this.title = title;
        this.fileName = fileName;
        this.fileType = fileType;
        this.document = document;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getBytes() {
        return document;
    }

    @Override
    public String getMimeType() {
        return fileType.mimeType;
    }

    public String getTitle() {
        return title;
    }

    public enum FileType {
        PDF("application/pdf"),
        TXT("text/plain");

        public final String mimeType;

        FileType(final String mimeType) {
            this.mimeType = mimeType;
        }
    }
}
