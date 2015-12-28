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

import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.internal.Confirmable;

import java.util.List;

import static no.digipost.signature.client.portal.PortalSignatureJobStatus.NO_CHANGES;

/**
 * Indicates a job which has got a new {@link PortalSignatureJobStatus status}
 * since the last time its status was queried.
 *
 * <h3>Confirmation</h3>
 *
 * When the client {@link Confirmable confirms} this, the job and its associated
 * resources will become unavailable through the Signature API.
 */
public class PortalSignatureJobStatusChanged implements Confirmable {


    /**
     * This instance indicates that there has been no status updates since the last poll request for
     * {@link PortalSignatureJobStatusChanged}. Its status is {@link PortalSignatureJobStatus#NO_CHANGES NO_CHANGES}.
     */
    public static final PortalSignatureJobStatusChanged NO_UPDATED_STATUS = new PortalSignatureJobStatusChanged(null, NO_CHANGES, null, null, null) {
        @Override
        public long getSignatureJobId() {
            throw new IllegalStateException(
                    "There were " + this + ", and querying the job ID is a programming error. " +
                    "Use the method is(" + PortalSignatureJobStatus.class.getSimpleName() + "." + NO_CHANGES.name() + ") " +
                    "to check if there were any status change before attempting to get any further information.");
        };

        @Override
        public String toString() {
            return "no signature jobs with updated status";
        }
    };

    private final Long signatureJobId;
    private final PortalSignatureJobStatus status;
    private final PAdESReference pAdESReference;
    private final ConfirmationReference confirmationReference;
    private final List<Signature> signatures;

    PortalSignatureJobStatusChanged(Long signatureJobId, PortalSignatureJobStatus status, ConfirmationReference confirmationReference, PAdESReference pAdESReference, List<Signature> signatures) {
        this.signatureJobId = signatureJobId;
        this.status = status;
        this.pAdESReference = pAdESReference;
        this.confirmationReference = confirmationReference;
        this.signatures = signatures;
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    public PortalSignatureJobStatus getStatus() {
        return status;
    }

    public boolean is(PortalSignatureJobStatus status) {
        return this.status == status;
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
        return "updated status for signature job with id " + signatureJobId + ": " + status;
    }

}
