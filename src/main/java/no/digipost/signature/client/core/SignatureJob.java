package no.digipost.signature.client.core;

import java.util.List;
import java.util.Optional;

public interface SignatureJob {

    // TODO: Remove this?
    Document getDocument();

    List<? extends Document> getDocuments();

    Optional<Sender> getSender();

    String getReference();

    Optional<AuthenticationLevel> getRequiredAuthentication();

    Optional<IdentifierInSignedDocuments> getIdentifierInSignedDocuments();

}
