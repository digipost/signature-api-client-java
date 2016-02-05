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
import no.digipost.signature.client.core.internal.Cancellable;
import no.digipost.signature.client.core.internal.Confirmable;

import java.util.List;

import static no.digipost.signature.client.portal.PortalJobStatus.NO_CHANGES;

/**
 * Indicates a job which has got a new {@link PortalJobStatus status}
 * since the last time its status was queried.
 *
 * <h3>Confirmation</h3>
 *
 * When the client {@link Confirmable confirms} this, the job and its associated
 * resources will become unavailable through the Signature API.
 */
public class PortalJobStatusChanged implements Confirmable, Cancellable {


    /**
     * This instance indicates that there has been no status updates since the last poll request for
     * {@link PortalJobStatusChanged}. Its status is {@link PortalJobStatus#NO_CHANGES NO_CHANGES}.
     */
    public static final PortalJobStatusChanged NO_UPDATED_STATUS = new PortalJobStatusChanged(null, NO_CHANGES, null, null, null, null) {
        @Override
        public long getSignatureJobId() {
            throw new IllegalStateException(
                    "There were " + this + ", and querying the job ID is a programming error. " +
                    "Use the method is(" + PortalJobStatus.class.getSimpleName() + "." + NO_CHANGES.name() + ") " +
                    "to check if there were any status change before attempting to get any further information.");
        };

        @Override
        public String toString() {
            return "no portal jobs with updated status";
        }
    };

    private final Long signatureJobId;
    private final PortalJobStatus status;
    private final PAdESReference pAdESReference;
    private final ConfirmationReference confirmationReference;
    private final CancellationUrl cancellationUrl;
    private final List<Signature> signatures;

    PortalJobStatusChanged(Long signatureJobId, PortalJobStatus status, ConfirmationReference confirmationReference, CancellationUrl cancellationUrl, PAdESReference pAdESReference, List<Signature> signatures) {
        this.signatureJobId = signatureJobId;
        this.status = status;
        this.cancellationUrl = cancellationUrl;
        this.pAdESReference = pAdESReference;
        this.confirmationReference = confirmationReference;
        this.signatures = signatures;
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    public PortalJobStatus getStatus() {
        return status;
    }

    public boolean is(PortalJobStatus status) {
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
    public CancellationUrl getCancellationUrl() {
        return cancellationUrl;
    }

    @Override
    public String toString() {
        return "updated status for portal job with id " + signatureJobId + ": " + status;
    }

}
