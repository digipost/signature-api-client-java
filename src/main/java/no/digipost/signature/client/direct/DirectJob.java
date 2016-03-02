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
package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.Signer;

import java.util.UUID;

public class DirectJob implements SignatureJob, WithExitUrls {

    private String reference;
    private Signer signer;
    private Document document;
    private String completionUrl;
    private String rejectionUrl;
    private String errorUrl;

    private DirectJob(Signer signer, Document document, String completionUrl, String rejectionUrl, String errorUrl) {
        this.signer = signer;
        this.document = document;
        this.completionUrl = completionUrl;
        this.rejectionUrl = rejectionUrl;
        this.errorUrl = errorUrl;
    }

    public String getReference() {
        return reference;
    }

    public Signer getSigner() {
        return signer;
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public String getCompletionUrl() {
        return completionUrl;
    }

    @Override
    public String getRejectionUrl() {
        return rejectionUrl;
    }

    @Override
    public String getErrorUrl() {
        return errorUrl;
    }

    public static Builder builder(Signer signer, Document document, WithExitUrls hasExitUrls) {
        return new Builder(signer, document, hasExitUrls.getCompletionUrl(), hasExitUrls.getRejectionUrl(), hasExitUrls.getErrorUrl());
    }

    /**
     * @deprecated Prefer the {@link #builder(Signer, Document, WithExitUrls) builder(Signer, Document, }{@link ExitUrls#of(String, String, String)})
     *             method.
     */
    @Deprecated
    public static Builder builder(Signer signer, Document document, String completionUrl, String rejectionUrl, String errorUrl) {
        return new Builder(signer, document, completionUrl, rejectionUrl, errorUrl);
    }

    public static class Builder {

        private final DirectJob target;
        private boolean built = false;

        public Builder(Signer signer, Document document, String completionUrl, String rejectionUrl, String errorUrl) {
            target = new DirectJob(signer, document, completionUrl, rejectionUrl, errorUrl);
        }

        public Builder withReference(UUID uuid) {
            return withReference(uuid.toString());
        }

        public Builder withReference(String reference) {
            target.reference = reference;
            return this;
        }

        public DirectJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }

}
