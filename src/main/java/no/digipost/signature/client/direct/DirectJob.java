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

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.motif.Singular;
import no.motif.single.Optional;

import java.util.UUID;

public class DirectJob implements SignatureJob, WithExitUrls {

    private String reference;
    private DirectSigner signer;
    private DirectDocument document;
    private String completionUrl;
    private String rejectionUrl;
    private String errorUrl;
    private Optional<Sender> sender = Singular.none();

    private DirectJob(DirectSigner signer, DirectDocument document, String completionUrl, String rejectionUrl, String errorUrl) {
        this.signer = signer;
        this.document = document;
        this.completionUrl = completionUrl;
        this.rejectionUrl = rejectionUrl;
        this.errorUrl = errorUrl;
    }

    @Override
    public String getReference() {
        return reference;
    }

    public DirectSigner getSigner() {
        return signer;
    }

    @Override
    public DirectDocument getDocument() {
        return document;
    }

    @Override
    public Optional<Sender> getSender() {
        return sender;
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

    /**
     * Create a new DirectJob.
     *
     * @param signer      The {@link DirectSigner} of the document.
     * @param document    The {@link DirectDocument} that should be signed.
     * @param hasExitUrls specifies the urls the user will be redirected back to upon completing/rejecting/failing
     *                    the signing ceremony. See {@link ExitUrls#of(String, String, String)}, and alternatively
     *                    {@link ExitUrls#singleExitUrl(String)}.
     *
     * @return a builder to further customize the job
     */
    public static Builder builder(DirectSigner signer, DirectDocument document, WithExitUrls hasExitUrls) {
        return new Builder(signer, document, hasExitUrls.getCompletionUrl(), hasExitUrls.getRejectionUrl(), hasExitUrls.getErrorUrl());
    }


    public static class Builder {

        private final DirectJob target;
        private boolean built = false;

        public Builder(DirectSigner signer, DirectDocument document, String completionUrl, String rejectionUrl, String errorUrl) {
            target = new DirectJob(signer, document, completionUrl, rejectionUrl, errorUrl);
        }

        public Builder withReference(UUID uuid) {
            return withReference(uuid.toString());
        }

        public Builder withReference(String reference) {
            target.reference = reference;
            return this;
        }

        /**
         * Set the sender for this specific signature job.
         * <p>
         * You may use {@link ClientConfiguration.Builder#globalSender(Sender)} to specify a global sender used for all signature jobs
         */
        public Builder withSender(Sender sender) {
            target.sender = Singular.optional(sender);
            return this;
        }

        public DirectJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }

}
