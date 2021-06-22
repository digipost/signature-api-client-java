package no.digipost.signature.client.core;

import java.util.List;
import java.util.Optional;

public interface SignatureJob {

    List<Document> getDocuments();

    Optional<Sender> getSender();

    String getReference();

    Optional<AuthenticationLevel> getRequiredAuthentication();

    Optional<IdentifierInSignedDocuments> getIdentifierInSignedDocuments();

}
