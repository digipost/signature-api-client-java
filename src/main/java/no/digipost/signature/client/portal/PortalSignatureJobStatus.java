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

import no.digipost.signature.client.core.Confirmable;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatus;

public enum PortalSignatureJobStatus {

    /**
     * When the client {@link Confirmable confirms} a job with this status,
     * the job and its associated resources will become unavailable through the Signature API.
     */
    SIGNED,

    /**
     * When the client {@link Confirmable confirms} a job with this status,
     * the job and its associated resources will become unavailable through the Signature API.
     */
    CANCELLED;

    public static PortalSignatureJobStatus fromXmlType(XMLPortalSignatureJobStatus xmlJobStatus) {
        switch (xmlJobStatus) {
            case SIGNED:
                return SIGNED;
            case CANCELLED:
                return CANCELLED;
            default:
                throw new IllegalArgumentException("Unexpected status: " + xmlJobStatus);
        }
    }
}
