package no.digipost.signature.client.core;

import no.digipost.signature.client.asice.ASiCEAttachable;

public abstract class Document implements ASiCEAttachable {

    private String title;
    private String fileName;
    private byte[] document;
    private FileType fileType;

    protected Document(final String title, final String fileName, final FileType fileType, final byte[] document) {
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
