package no.digipost.signature.client.portal;

import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.internal.JobCustomizations;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;


/**
 * Signature job with document(s) to be signed by
 * one or more signers in portal flow.
 */
public class PortalJob implements SignatureJob {

    private final List<PortalDocument> documents;
    private final List<PortalSigner> signers;
    private final String title;
    private Optional<String> nonsensitiveTitle = Optional.empty();
    private Optional<String> description = Optional.empty();
    private String reference;
    private Optional<Instant> activationTime = Optional.empty();
    private Optional<Duration> available = Optional.empty();
    private Optional<Sender> sender = Optional.empty();
    private Optional<AuthenticationLevel> requiredAuthentication = Optional.empty();
    private Optional<IdentifierInSignedDocuments> identifierInSignedDocuments = Optional.empty();

    private PortalJob(String title, List<PortalDocument> documents, List<PortalSigner> signers) {
        this.title = title;
        this.documents = unmodifiableList(new ArrayList<>(documents));
        this.signers = unmodifiableList(new ArrayList<>(signers));
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public List<PortalDocument> getDocuments() {
        return documents;
    }

    @Override
    public Optional<Sender> getSender() {
        return sender;
    }

    @Override
    public Optional<AuthenticationLevel> getRequiredAuthentication() {
        return requiredAuthentication;
    }

    @Override
    public Optional<IdentifierInSignedDocuments> getIdentifierInSignedDocuments() {
        return identifierInSignedDocuments;
    }

    public List<PortalSigner> getSigners() {
        return signers;
    }

    public Optional<Instant> getActivationTime() {
        return activationTime;
    }

    public Optional<Duration> getAvailable() {
        return available;
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getNonsensitiveTitle() {
        return nonsensitiveTitle;
    }

    public Optional<String> getDescription() {
        return description;
    }


    /**
     * Create a new signature job for portal flow.
     *
     * @param document    The {@link PortalDocument document} that should be signed.
     * @param signer      The {@link PortalSigner signer} of the document.
     *
     * @return a builder to further customize the job
     */
    public static Builder builder(String title, PortalDocument document, PortalSigner signer) {
        return builder(title, singletonList(document), singletonList(signer));
    }

    /**
     * Create a new signature job for portal flow.
     *
     * @param documents   The {@link PortalDocument documents} that should be signed.
     * @param signers     The {@link PortalSigner signers} of the document.
     *
     * @return a builder to further customize the job
     */
    public static Builder builder(String title, List<PortalDocument> documents, List<PortalSigner> signers) {
        return new Builder(title, documents, signers);
    }

    public static class Builder implements JobCustomizations<Builder> {

        private final PortalJob target;
        private boolean built = false;

        private Builder(String title, List<PortalDocument> documents, List<PortalSigner> signers) {
            target = new PortalJob(title, documents, signers);
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
        public Builder requireAuthentication(AuthenticationLevel minimumLevel) {
            target.requiredAuthentication = Optional.of(minimumLevel);
            return this;
        }

        @Override
        public Builder withIdentifierInSignedDocuments(IdentifierInSignedDocuments identifier) {
            target.identifierInSignedDocuments = Optional.of(identifier);
            return this;
        }

        public Builder withActivationTime(Instant activationTime) {
            target.activationTime = Optional.of(activationTime);
            return this;
        }

        public Builder availableFor(Duration duration) {
            target.available = Optional.of(duration);
            return this;
        }

        public Builder withNonsensitiveTitle(String nonsensitiveTitle) {
            target.nonsensitiveTitle = Optional.of(nonsensitiveTitle);
            return this;
        }

        public Builder withDescription(String description) {
            target.description = Optional.of(description);
            return this;
        }

        public PortalJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }


    }

}

