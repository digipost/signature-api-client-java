package no.digipost.signature.client.core;

import java.util.Optional;

public interface SignatureJob {

    Document getDocument();

    Optional<Sender> getSender();

    String getReference();

    Optional<AuthenticationLevel> getRequiredAuthentication();

    Optional<IdentifierInSignedDocuments> getIdentifierInSignedDocuments();

}
