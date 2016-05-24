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

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

public final class DirectJobStatus {

    /**
     * The user decided to reject to sign the document, and has been redirected to the
     * rejection-url provided in the direct-signature-job-request's exit-urls.
     */
    public static final DirectJobStatus REJECTED = new DirectJobStatus("REJECTED");

    /**
     * The user didn't sign the document before the job expired.
     */
    public static final DirectJobStatus EXPIRED = new DirectJobStatus("EXPIRED");

    /**
     * The document has been signed, and the signer has been redirected to the
     * completion-url provided in the direct-signature-job-request's exit-urls.
     * The signed document artifacts can be downloaded by following the appropriate
     * urls in the direct-signature-job-status-response.
     */
    public static final DirectJobStatus SIGNED = new DirectJobStatus("SIGNED");

    /**
     * An unexpected error occured during the signing ceremony, and the user has been redirected to the
     * error-url provided in the direct-signature-job-request's exit-urls.
     */
    public static final DirectJobStatus FAILED = new DirectJobStatus("FAILED");

	/**
     * There has not been any changes since the last received status change.
     */
    public static final DirectJobStatus NO_CHANGES = new DirectJobStatus("NO_CHANGES");

    private static final List<DirectJobStatus> KNOWN_STATUSES = asList(
            REJECTED,
            EXPIRED,
            SIGNED,
            FAILED
    );

    private final String identifier;

    public DirectJobStatus(String identifier) {
        this.identifier = identifier;
    }

    public static DirectJobStatus fromXmlType(String xmlDirectJobStatus) {
        for (DirectJobStatus status : KNOWN_STATUSES) {
            if (status.is(xmlDirectJobStatus)) {
                return status;
            }
        }

        return new DirectJobStatus(xmlDirectJobStatus);
    }

    private boolean is(String xmlDirectJobStatus) {
        return this.identifier.equals(xmlDirectJobStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DirectJobStatus) {
            DirectJobStatus that = (DirectJobStatus) o;
            return Objects.equals(identifier, that.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }
}
