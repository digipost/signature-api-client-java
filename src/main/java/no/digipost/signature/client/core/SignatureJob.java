package no.digipost.signature.client.core;

import java.util.List;
import java.util.Optional;

public interface SignatureJob {

    default Document getDocument() {
        List<? extends Document> documents = getDocuments();
        if (documents.size() > 1) {
            throw new RuntimeException("Expected one document, but found "+ documents.size());
        }
        return getDocuments().stream().findFirst().orElseThrow(() -> new RuntimeException("Expected one document, but found none"));
    };

    List<? extends Document> getDocuments();

    Optional<Sender> getSender();

    String getReference();

    Optional<AuthenticationLevel> getRequiredAuthentication();

    Optional<IdentifierInSignedDocuments> getIdentifierInSignedDocuments();

}
