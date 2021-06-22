package no.digipost.signature.client.portal;

import no.digipost.signature.client.core.DocumentType;

import static java.util.Objects.requireNonNull;
import static no.digipost.signature.client.core.DocumentType.PDF;


public class PortalDocument {

    public static Builder builder(String title, byte[] documentContent) {
        return new Builder(title, documentContent);
    }


    public final String title;
    public final DocumentType type;
    public final byte[] content;

    private PortalDocument(String title, DocumentType type, byte[] content) {
        this.title = title;
        this.type = type;
        this.content = content;
    }


    public static class Builder {

        private String title;
        private DocumentType type = PDF;
        private byte[] content;

        public Builder(String title, byte[] content) {
            this.title = requireNonNull(title, "title");
            this.content = requireNonNull(content, "document content");
        }

        public Builder type(DocumentType type) {
            this.type = requireNonNull(type, "document type");
            return this;
        }

        public PortalDocument build() {
            return new PortalDocument(title, type, content);
        }
    }
}
