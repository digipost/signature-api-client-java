package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.*;
import no.digipost.signature.client.core.internal.JobCustomizations;

import java.net.URI;
import java.util.*;

import static java.util.Collections.unmodifiableList;

public class DirectJob implements SignatureJob, WithExitUrls {

    private String reference;
    private List<DirectSigner> signers;
    private DirectDocument document;
    private URI completionUrl;
    private URI rejectionUrl;
    private URI errorUrl;
    private Optional<Sender> sender = Optional.empty();
    private Optional<StatusRetrievalMethod> statusRetrievalMethod = Optional.empty();
    private Optional<AuthenticationLevel> requiredAuthentication = Optional.empty();
    private Optional<IdentifierInSignedDocuments> identifierInSignedDocuments = Optional.empty();

    private DirectJob(List<DirectSigner> signers, DirectDocument document, URI completionUrl, URI rejectionUrl, URI errorUrl) {
        this.signers = unmodifiableList(new ArrayList<>(signers));
        this.document = document;
        this.completionUrl = completionUrl;
        this.rejectionUrl = rejectionUrl;
        this.errorUrl = errorUrl;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public DirectDocument getDocument() {
        return document;
    }

    @Override
    public List<? extends Document> getDocuments() {
        return unmodifiableList(Collections.singletonList(getDocument()));
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


    /**
     * Create a new DirectJob.
     *
     * @param document    The {@link DirectDocument} that should be signed.
     * @param hasExitUrls specifies the urls the user will be redirected back to upon completing/rejecting/failing
     *                    the signing ceremony. See {@link ExitUrls#of(URI, URI, URI)}, and alternatively
     *                    {@link ExitUrls#singleExitUrl(URI)}.
     * @param signers     The {@link DirectSigner DirectSigners} of the document.
     *
     * @return a builder to further customize the job
     * @see DirectJob#builder(DirectDocument, WithExitUrls, List)
     */
    public static Builder builder(DirectDocument document, WithExitUrls hasExitUrls, DirectSigner... signers) {
        return builder(document, hasExitUrls, Arrays.asList(signers));
    }


    /**
     * Create a new DirectJob.
     *
     * @param document    The {@link DirectDocument} that should be signed.
     * @param hasExitUrls specifies the urls the user will be redirected back to upon completing/rejecting/failing
     *                    the signing ceremony. See {@link ExitUrls#of(URI, URI, URI)}, and alternatively
     *                    {@link ExitUrls#singleExitUrl(URI)}.
     * @param signers     The {@link DirectSigner DirectSigners} of the document.
     *
     * @return a builder to further customize the job
     * @see DirectJob#builder(DirectDocument, WithExitUrls, DirectSigner...)
     */
    public static Builder builder(DirectDocument document, WithExitUrls hasExitUrls, List<DirectSigner> signers) {
        return new Builder(signers, document, hasExitUrls.getCompletionUrl(), hasExitUrls.getRejectionUrl(), hasExitUrls.getErrorUrl());
    }

    public static class Builder implements JobCustomizations<Builder> {

        private final DirectJob target;
        private boolean built = false;

        private Builder(List<DirectSigner> signers, DirectDocument document, URI completionUrl, URI rejectionUrl, URI errorUrl) {
            target = new DirectJob(signers, document, completionUrl, rejectionUrl, errorUrl);
        }

        @Override
        public Builder withReference(UUID uuid) {
            return withReference(uuid.toString());
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
