package no.digipost.signature.client.core;

import no.digipost.signature.client.core.internal.MaySpecifySender;

import java.util.List;
import java.util.Optional;

public interface SignatureJob extends MaySpecifySender {

    List<Document> getDocuments();

    String getReference();

    Optional<AuthenticationLevel> getRequiredAuthentication();

    Optional<IdentifierInSignedDocuments> getIdentifierInSignedDocuments();

}
