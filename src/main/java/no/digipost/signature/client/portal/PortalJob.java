package no.digipost.signature.client.portal;

import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.internal.JobCustomizations;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.unmodifiableList;


public class PortalJob implements SignatureJob {

    private final List<PortalSigner> signers;
    private final List<PortalDocument> documents;
    private String title;
    private Optional<String> nonsensitiveTitle = Optional.empty();
    private Optional<String> description = Optional.empty();
    private String reference;
    private Optional<Instant> activationTime = Optional.empty();
    private Long availableSeconds;
    private Optional<Sender> sender = Optional.empty();
    private Optional<AuthenticationLevel> requiredAuthentication = Optional.empty();
    private Optional<IdentifierInSignedDocuments> identifierInSignedDocuments = Optional.empty();

    private PortalJob(String title, List<PortalSigner> signers, List<PortalDocument> documents) {
        this.title = title;
        this.signers = unmodifiableList(new ArrayList<>(signers));
        this.documents = unmodifiableList(new ArrayList<>(documents));
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

    public Long getAvailableSeconds() {
        return availableSeconds;
    }


    public static Builder builder(String title, PortalDocument document, PortalSigner... signers) {
        return builder(title, Collections.singletonList(document), Arrays.asList(signers));
    }

    public static Builder builder(String title, PortalDocument document, List<PortalSigner> signers) {
        return builder(title, Collections.singletonList(document), signers);
    }

    public static Builder builder(String title, List<PortalDocument> documents, List<PortalSigner> signers) {
        return new Builder(title, signers, documents);
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

    public static class Builder implements JobCustomizations<Builder> {

        private final PortalJob target;
        private boolean built = false;

        private Builder(String title, List<PortalSigner> signers, List<PortalDocument> documents) {
            target = new PortalJob(title, signers, documents);
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

        public Builder availableFor(long duration, TimeUnit unit) {
            target.availableSeconds = unit.toSeconds(duration);
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

