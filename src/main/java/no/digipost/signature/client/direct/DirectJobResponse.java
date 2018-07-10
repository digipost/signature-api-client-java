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

import no.digipost.signature.client.direct.RedirectUrls.RedirectUrl;

import java.util.List;

public class DirectJobResponse {
    private final long signatureJobId;
    private final String reference;
    private final RedirectUrls redirectUrls;
    private final String statusUrl;

    public DirectJobResponse(long signatureJobId, String reference, List<RedirectUrl> redirectUrls, String statusUrl) {
        this.signatureJobId = signatureJobId;
        this.reference = reference;
        this.redirectUrls = new RedirectUrls(redirectUrls);
        this.statusUrl = statusUrl;
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    /**
     * @return the signature job's custom reference as specified upon
     * {@link DirectJob.Builder#withReference(String) creation}. May be {@code null}.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Gets the only redirect URL for this job.
     * Convenience method for retrieving the redirect URL for jobs with exactly one signer.
     * @throws IllegalStateException if there are multiple redirect URLs
     * @see #getRedirectUrls()
     */
    public String getSingleRedirectUrl() {
        return redirectUrls.getSingleRedirectUrl();
    }

    public RedirectUrls getRedirectUrls() {
        return redirectUrls;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

}
