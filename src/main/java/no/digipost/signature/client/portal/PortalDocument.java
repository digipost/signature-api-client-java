package no.digipost.signature.client.portal;

import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.DocumentType;

import static java.util.Objects.requireNonNull;
import static no.digipost.signature.client.core.DocumentType.PDF;
import static no.digipost.signature.client.core.internal.FileName.reduceToFileNameSafeChars;


public class PortalDocument extends Document {

    public static Builder builder(String title, byte[] document) {
        return new Builder(title, document);
    }


    private PortalDocument(String title, DocumentType documentType, String fileName, byte[] document) {
        super(title, documentType, fileName, document);
    }

    public static class Builder {

        private String title;
        private DocumentType documentType = PDF;
        private String fileName;
        private byte[] document;

        public Builder(String title, byte[] document) {
            this.title = requireNonNull(title, "title");
            this.document = requireNonNull(document, "document bytes");
        }

        public Builder type(DocumentType documentType) {
            this.documentType = requireNonNull(documentType, "document type");
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public PortalDocument build() {
            return new PortalDocument(
                    title, documentType,
                    fileName == null ? reduceToFileNameSafeChars(title) + "." + documentType.getFileExtension(): fileName,
                    document);
        }
    }
}
