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
import no.digipost.signature.client.core.Signer;

public class SignatureJob {

    private String id;
    private Signer signer;
    private Document document;
    private String completionUrl;
    private String cancellationUrl;

    private SignatureJob(final String id, final Signer signer, final Document document, final String completionUrl, final String cancellationUrl) {
        this.id = id;
        this.signer = signer;
        this.document = document;
        this.completionUrl = completionUrl;
        this.cancellationUrl = cancellationUrl;
    }

    public String getId() {
        return id;
    }

    public Signer getSigner() {
        return signer;
    }

    public Document getDocument() {
        return document;
    }

    public String getCompletionUrl() {
        return completionUrl;
    }

    public String getCancellationUrl() {
        return cancellationUrl;
    }

    public static Builder builder(final String id, final Signer signer, final Document document, final String completionUrl, final String cancellationUrl) {
        return new Builder(id, signer, document, completionUrl, cancellationUrl);
    }

    public static class Builder {

        private final SignatureJob target;
        private boolean built = false;

        public Builder(final String id, final Signer signer, final Document document, final String completionUrl, final String cancellationUrl) {
            target = new SignatureJob(id, signer, document, completionUrl, cancellationUrl);
        }

        public SignatureJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }
}
