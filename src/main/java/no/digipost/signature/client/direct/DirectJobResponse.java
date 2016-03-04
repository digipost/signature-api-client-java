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

public class DirectJobResponse {
    private long signatureJobId;
    private String redirectUrl;
    private String statusUrl;

    public DirectJobResponse(long signatureJobId, String redirectUrl, String statusUrl) {
        this.signatureJobId = signatureJobId;
        this.redirectUrl = redirectUrl;
        this.statusUrl = statusUrl;
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * Get the reference (URL) to {@link DirectClient#getStatus(StatusReference) query the status}
     * of this job.
     * <p>
     * When the signer has completed/aborted/failed the signature ceremony and is redirected to
     * one of the {@link ExitUrls} provided when the job was created, this token is provided as an added
     * query parameter ({@link StatusReference#STATUS_QUERY_TOKEN_PARAM_NAME}) for the URL. The token needs to
     * be consumed by the system handling where the user is redirected to, and consequently provided here to
     * resolve a valid URL to query for the job's status.
     *
     * @param statusQueryToken the token required to be allowed to ask for the job's status.
     *
     * @return the {@link StatusReference} for this job
     */
    public StatusReference getStatusUrl(String statusQueryToken) {
        return new StatusReference(statusUrl, statusQueryToken);
    }

}
