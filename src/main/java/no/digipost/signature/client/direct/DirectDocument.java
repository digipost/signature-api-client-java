package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.DocumentType;

import static java.util.Objects.requireNonNull;
import static no.digipost.signature.client.core.DocumentType.PDF;


public class DirectDocument {

    public static Builder builder(String title, byte[] document) {
        return new Builder(title, document);
    }


    public final String title;
    public final DocumentType type;
    public final byte[] document;

    private DirectDocument(String title, DocumentType type, byte[] document) {
        this.title = title;
        this.type = type;
        this.document = document;
    }


    public static class Builder {

        private String title;
        private DocumentType documentType = PDF;
        private byte[] document;

        public Builder(String title, byte[] document) {
            this.title = requireNonNull(title, "title");
            this.document = requireNonNull(document, "document bytes");
        }

        public Builder type(DocumentType documentType) {
            this.documentType = requireNonNull(documentType, "document type");
            return this;
        }

        public DirectDocument build() {
            return new DirectDocument(title, documentType, document);
        }
    }
}
