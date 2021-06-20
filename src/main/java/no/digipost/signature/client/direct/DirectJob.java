package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.internal.JobCustomizations;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static no.digipost.signature.client.core.internal.FileName.reduceToFileNameSafeChars;


/**
 * Signature job with document(s) to be signed by
 * one or more signers in direct flow.
 */
public class DirectJob implements SignatureJob, WithExitUrls {

    private final List<DirectDocument> documents;
    private final List<DirectSigner> signers;
    private final String title;
    private final URI completionUrl;
    private final URI rejectionUrl;
    private final URI errorUrl;
    private Optional<String> description = Optional.empty();
    private String reference;
    private Optional<Sender> sender = Optional.empty();
    private Optional<StatusRetrievalMethod> statusRetrievalMethod = Optional.empty();
    private Optional<AuthenticationLevel> requiredAuthentication = Optional.empty();
    private Optional<IdentifierInSignedDocuments> identifierInSignedDocuments = Optional.empty();

    private DirectJob(String title, List<DirectDocument> documents, List<DirectSigner> signers, URI completionUrl, URI rejectionUrl, URI errorUrl) {
        this.title = title;
        this.documents = unmodifiableList(new ArrayList<>(documents));
        this.signers = unmodifiableList(new ArrayList<>(signers));
        this.completionUrl = completionUrl;
        this.rejectionUrl = rejectionUrl;
        this.errorUrl = errorUrl;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public List<Document> getDocuments() {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < this.documents.size(); i++) {
            DirectDocument doc = this.documents.get(i);
            documents.add(new Document(doc.title, doc.type.getMediaType(),
                    format("%04d", i) + "_" + reduceToFileNameSafeChars(doc.title) + "." + doc.type.getFileExtension(),
                    doc.document));
        }
        return documents;
    }

    @Override
    public Optional<Sender> getSender() {
        return sender;
    }

    @Override
    public URI getCompletionUrl() {
        return completionUrl;
    }

    @Override
    public URI getRejectionUrl() {
        return rejectionUrl;
    }

    @Override
    public URI getErrorUrl() {
        return errorUrl;
    }

    @Override
    public Optional<AuthenticationLevel> getRequiredAuthentication() {
        return requiredAuthentication;
    }

    @Override
    public Optional<IdentifierInSignedDocuments> getIdentifierInSignedDocuments() {
        return identifierInSignedDocuments;
    }

    public List<DirectSigner> getSigners() {
        return signers;
    }

    public Optional<StatusRetrievalMethod> getStatusRetrievalMethod() {
        return statusRetrievalMethod;
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getDescription() {
        return description;
    }


    /**
     * Create a new signature job for direct flow.
     *
     * @param document    The {@link DirectDocument document} that should be signed.
     * @param hasExitUrls specifies the URLs the user will be redirected back to upon completing/rejecting/failing
     *                    to sign the document. See {@link ExitUrls#of(URI, URI, URI)}, and alternatively
     *                    {@link ExitUrls#singleExitUrl(URI)}.
     * @param signer      The {@link DirectSigner signer} of the document.
     *
     * @return a builder to further customize the job
     */
    public static Builder builder(String title, DirectDocument document, DirectSigner signer, WithExitUrls hasExitUrls) {
        return builder(title, singletonList(document), singletonList(signer), hasExitUrls);
    }

    /**
     * Create a new signature job for direct flow.
     *
     * @param documents   The {@link DirectDocument document} that should be signed.
     * @param hasExitUrls specifies the URLs the user will be redirected back to upon completing/rejecting/failing
     *                    to sign the documents. See {@link ExitUrls#of(URI, URI, URI)}, and alternatively
     *                    {@link ExitUrls#singleExitUrl(URI)}.
     * @param signers     The {@link DirectSigner signers} of the document.
     *
     * @return a builder to further customize the job
     */
    public static Builder builder(String title, List<DirectDocument> documents, List<DirectSigner> signers, WithExitUrls hasExitUrls) {
        return new Builder(title, documents, signers, hasExitUrls.getCompletionUrl(), hasExitUrls.getRejectionUrl(), hasExitUrls.getErrorUrl());
    }

    public static class Builder implements JobCustomizations<Builder> {

        private final DirectJob target;
        private boolean built = false;

        private Builder(String title, List<DirectDocument> documents, List<DirectSigner> signers, URI completionUrl, URI rejectionUrl, URI errorUrl) {
            target = new DirectJob(title, documents, signers, completionUrl, rejectionUrl, errorUrl);
        }

        @Override
        public Builder withReference(UUID uuid) {
            return withReference(uuid.toString());
        }

        public Builder withDescription(String description) {
            target.description = Optional.of(description);
            return this;
        }

        @Override
        public Builder withReference(String reference) {
            target.reference = reference;
            return this;
        }

        @Override
        public Builder withSender(Sender sender) {
            target.sender = Optional.of(sender);
            return this;
        }

        @Override
        public Builder requireAuthentication(AuthenticationLevel level) {
            target.requiredAuthentication = Optional.of(level);
            return this;
        }

        @Override
        public Builder withIdentifierInSignedDocuments(IdentifierInSignedDocuments identifier) {
            target.identifierInSignedDocuments = Optional.of(identifier);
            return this;
        }

        public Builder retrieveStatusBy(StatusRetrievalMethod statusRetrievalMethod) {
            target.statusRetrievalMethod = Optional.of(statusRetrievalMethod);
            return this;
        }

        public DirectJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }

}
