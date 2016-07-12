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

import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.internal.Confirmable;

import java.util.List;

import static no.digipost.signature.client.direct.DirectJobStatus.NO_CHANGES;


public class DirectJobStatusResponse implements Confirmable {

    /**
     * This instance indicates that there has been no status updates since the last poll request for
     * {@link DirectJobStatusResponse}. Its status is {@link DirectJobStatus#NO_CHANGES NO_CHANGES}.
     */
    public static final DirectJobStatusResponse NO_UPDATED_STATUS = new DirectJobStatusResponse(null, NO_CHANGES, null, null, null) {
        @Override
        public long getSignatureJobId() {
            throw new IllegalStateException(
                    "There were " + this + ", and querying the job ID is a programming error. " +
                            "Use the method is(" + DirectJobStatusResponse.class.getSimpleName() + "." + NO_CHANGES.name() + ") " +
                            "to check if there were any status change before attempting to get any further information.");
        };

        @Override
        public String toString() {
            return "no direct jobs with updated status";
        }
    };


    private final Long signatureJobId;
    private final DirectJobStatus status;
    private final ConfirmationReference confirmationReference;
    private final List<Signature> signatures;
    private final PAdESReference pAdESReference;

    public DirectJobStatusResponse(Long signatureJobId, DirectJobStatus signatureJobStatus, ConfirmationReference confirmationUrl, List<Signature> signatures, PAdESReference pAdESReference) {
        this.signatureJobId = signatureJobId;
        this.status = signatureJobStatus;
        this.confirmationReference = confirmationUrl;
        this.signatures = signatures;
        this.pAdESReference = pAdESReference;
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    public DirectJobStatus getStatus() {
        return status;
    }

    public boolean is(DirectJobStatus status) {
        return this.status == status;
    }

    public boolean isPAdESAvailable() {
        return pAdESReference != null;
    }

    public PAdESReference getpAdESUrl() {
        return pAdESReference;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    @Override
    public ConfirmationReference getConfirmationReference() {
        return confirmationReference;
    }

    @Override
    public String toString() {
        return "status for direct job with ID " + signatureJobId + ": " + status;
    }

}
