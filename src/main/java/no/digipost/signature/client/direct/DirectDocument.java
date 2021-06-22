package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.DocumentType;

import static java.util.Objects.requireNonNull;
import static no.digipost.signature.client.core.DocumentType.PDF;


public class DirectDocument {

    public static Builder builder(String title, byte[] documentContent) {
        return new Builder(title, documentContent);
    }


    public final String title;
    public final DocumentType type;
    public final byte[] content;

    private DirectDocument(String title, DocumentType type, byte[] content) {
        this.title = title;
        this.type = type;
        this.content = content;
    }


    public static class Builder {

        private String title;
        private DocumentType type = PDF;
        private byte[] content;

        private Builder(String title, byte[] content) {
            this.title = requireNonNull(title, "title");
            this.content = requireNonNull(content, "document content");
        }

        public Builder type(DocumentType type) {
            this.type = requireNonNull(type, "document type");
            return this;
        }

        public DirectDocument build() {
            return new DirectDocument(title, type, content);
        }
    }
}
