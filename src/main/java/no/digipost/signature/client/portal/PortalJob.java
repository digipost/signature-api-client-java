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

import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.Signer;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.unmodifiableList;


public class PortalJob implements SignatureJob {

    private String reference;
    private List<Signer> signers;
    private Document document;
    private Date activationTime;
    private BigInteger availableSeconds;


    private PortalJob(List<Signer> signers, Document document) {
        this.signers = unmodifiableList(new ArrayList<>(signers));
        this.document = document;
    }

    public String getReference() {
        return reference;
    }

    public List<Signer> getSigners() {
        return signers;
    }

    @Override
    public Document getDocument() {
        return document;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public BigInteger getAvailableSeconds() {
        return availableSeconds;
    }


    public static Builder builder(Document document, Signer... signers) {
        return builder(document, Arrays.asList(signers));
    }

    public static Builder builder(Document document, List<Signer> signers) {
        return new Builder(signers, document);
    }

    public static class Builder {

        private final PortalJob target;
        private boolean built = false;

        private Builder(List<Signer> signers, Document document) {
            target = new PortalJob(signers, document);
        }

        public Builder withReference(UUID uuid) {
            return withReference(uuid.toString());
        }

        public Builder withReference(String reference) {
            target.reference = reference;
            return this;
        }

        public Builder withActivationTime(Date activationTime) {
            target.activationTime = activationTime;
            return this;
        }

        public Builder availableFor(long duration, TimeUnit unit) {
            target.availableSeconds = BigInteger.valueOf(unit.toSeconds(duration));
            return this;
        }

        public PortalJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }

    }

}

