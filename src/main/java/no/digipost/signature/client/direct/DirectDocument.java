package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.Document;

import static no.digipost.signature.client.core.Document.FileType.PDF;

public class DirectDocument extends Document {

    private DirectDocument(String title, String fileName, FileType fileType, byte[] document) {
        super(title, fileName, fileType, document);
    }

    public static Builder builder(String title, String fileName, byte[] document) {
        return new Builder(title, fileName, document);
    }

    public static class Builder {

        private String title;
        private String fileName;
        private byte[] document;
        private FileType fileType = PDF;

        public Builder(String title, String fileName, byte[] document) {
            this.title = title;
            this.fileName = fileName;
            this.document = document;
        }

        public Builder fileType(FileType fileType) {
            this.fileType = fileType;
            return this;
        }

        public DirectDocument build() {
            return new DirectDocument(title, fileName, fileType, document);
        }
    }
}
