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
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.Confirmable;

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
    private final XAdESReference xAdESUrl;
    private final PAdESReference pAdESUrl;
    private final ConfirmationReference confirmationReference;

    PortalSignatureJobStatusChanged(Long signatureJobId, PortalSignatureJobStatus status, String xAdESUrl, String pAdESUrl, String confirmationUrl) {
        this.signatureJobId = signatureJobId;
        this.status = status;
        this.xAdESUrl = new XAdESReference(xAdESUrl);
        this.pAdESUrl = new PAdESReference(pAdESUrl);
        this.confirmationReference = new ConfirmationReference(confirmationUrl);
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

    public XAdESReference getxAdESUrl() {
        return xAdESUrl;
    }

    public PAdESReference getpAdESUrl() {
        return pAdESUrl;
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
