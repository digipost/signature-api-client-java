package no.digipost.signature.client.core;

import no.digipost.signature.client.asice.ASiCEAttachable;

public abstract class Document implements ASiCEAttachable {

    private final String title;
    private final DocumentType documentType;
    private final String fileName;
    private final byte[] document;

    protected Document(String title, DocumentType documentType, String fileName, byte[] document) {
        this.title = title;
        this.documentType = documentType;
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
    public Type getType() {
        return documentType;
    }

    @Override
    public byte[] getBytes() {
        return document;
    }

}
