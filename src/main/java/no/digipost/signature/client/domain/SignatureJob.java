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
package no.digipost.signature.client.domain;

public class SignatureJob {

    private String signer;
    private Document document;
    private String completionUrl;

    private SignatureJob(final String signer, final Document document, final String completionUrl) {
        this.signer = signer;
        this.document = document;
        this.completionUrl = completionUrl;
    }

    public String getSigner() {
        return signer;
    }

    public Document getDocument() {
        return document;
    }

    public String getCompletionUrl() {
        return completionUrl;
    }

    public static Builder builder(final String signatar, final Document document, final String completionUrl) {
        return new Builder(signatar, document, completionUrl);
    }

    public static class Builder {

        private final SignatureJob target;
        private boolean built = false;

        public Builder(final String signer, final Document document, final String completionUrl) {
            target = new SignatureJob(signer, document, completionUrl);
        }

        public SignatureJob build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }
}
