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

import no.digipost.signering.schema.v1.XMLDirectSignatureJobStatus;

public enum SignatureJobStatus {

    /**
     * The signature job is created, and the receiver is currently doing the
     * signing ceremony.
     */
    CREATED,

    /**
     * The document(s) of the job has been signed by the receiver.
     */
    SIGNED,

    /**
     * The signature job has been cancelled.
     */
    CANCELLED;

    public static SignatureJobStatus fromXmlType(XMLDirectSignatureJobStatus xmlJobStatus) {
        switch (xmlJobStatus) {
            case CREATED:
                return CREATED;
            case SIGNED:
                return SIGNED;
            case CANCELLED:
                return CANCELLED;
            default:
                throw new IllegalArgumentException("Unexpected status: " + xmlJobStatus);
        }
    }

}
