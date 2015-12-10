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
import no.digipost.signature.client.core.Signer;


public class PortalSignatureJob {

    private String reference;
    private Signer signer;
    private Document document;

    private PortalSignatureJob(Signer signer, Document document) {
        this.signer = signer;
        this.document = document;
    }

    public String getReference() {
        return reference;
    }

    public Signer getSigner() {
        return signer;
    }

    public Document getDocument() {
        return document;
    }

    public static Builder builder(Signer signer, Document document) {
        return new Builder(signer, document);
    }

    public static class Builder {

        private final PortalSignatureJob target;
        private boolean built = false;

        public Builder(Signer signer, Document document) {
            target = new PortalSignatureJob(signer, document);
        }

        public Builder withReference(String reference) {
            target.reference = reference;
            return this;
        }

        public PortalSignatureJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }

}

