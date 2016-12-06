/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client.portal;

import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.internal.JobCustomizations;
import no.motif.Singular;
import no.motif.single.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.unmodifiableList;
import static no.motif.Singular.optional;


public class PortalJob implements SignatureJob {

    private final List<PortalSigner> signers;
    private final PortalDocument document;
    private String reference;
    private Date activationTime;
    private Long availableSeconds;
    private Optional<Sender> sender = Singular.none();
    private Optional<AuthenticationLevel> requiredAuthentication = Singular.none();

    private PortalJob(List<PortalSigner> signers, PortalDocument document) {
        this.signers = unmodifiableList(new ArrayList<>(signers));
        this.document = document;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public PortalDocument getDocument() {
        return document;
    }

    @Override
    public Optional<Sender> getSender() {
        return sender;
    }

    @Override
    public Optional<AuthenticationLevel> getRequiredAuthentication() {
        return requiredAuthentication;
    }

    public List<PortalSigner> getSigners() {
        return signers;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public Long getAvailableSeconds() {
        return availableSeconds;
    }


    public static Builder builder(PortalDocument document, PortalSigner... signers) {
        return builder(document, Arrays.asList(signers));
    }

    public static Builder builder(PortalDocument document, List<PortalSigner> signers) {
        return new Builder(signers, document);
    }

    public static class Builder implements JobCustomizations<Builder> {

        private final PortalJob target;
        private boolean built = false;

        private Builder(List<PortalSigner> signers, PortalDocument document) {
            target = new PortalJob(signers, document);
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
            target.sender = optional(sender);
            return this;
        }

        @Override
        public Builder requireAuthentication(AuthenticationLevel minimumLevel) {
            target.requiredAuthentication = optional(minimumLevel);
            return this;
        }

        public Builder withActivationTime(Date activationTime) {
            target.activationTime = activationTime;
            return this;
        }

        public Builder availableFor(long duration, TimeUnit unit) {
            target.availableSeconds = unit.toSeconds(duration);
            return this;
        }

        public PortalJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }


    }

}

