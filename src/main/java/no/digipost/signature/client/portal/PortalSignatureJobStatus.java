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

import no.digipost.signature.client.core.internal.Confirmable;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatus;

public enum PortalSignatureJobStatus {


	/**
     * When the client {@link Confirmable confirms} a job with this status,
     * the job is removed from the queue and will not be returned upon subsequent polling,
     * until the status has changed again.
     */
    PARTIALLY_COMPLETED,

    /**
     * When the client {@link Confirmable confirms} a job with this status,
     * the job and its associated resources will become unavailable through the Signature API.
     */
    COMPLETED,

    /**
     * There has not been any changes since the last received status change.
     */
    NO_CHANGES;

    public static PortalSignatureJobStatus fromXmlType(XMLPortalSignatureJobStatus xmlJobStatus) {
        switch (xmlJobStatus) {
            case PARTIALLY_COMPLETED:
                return PARTIALLY_COMPLETED;
            case COMPLETED:
                return COMPLETED;
            default:
                throw new IllegalArgumentException("Unexpected status: " + xmlJobStatus);
        }
    }
}
