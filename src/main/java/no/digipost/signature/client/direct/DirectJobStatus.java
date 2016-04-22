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

import no.digipost.signature.api.xml.XMLDirectSignatureJobStatus;

public enum DirectJobStatus {

    /**
     * The document(s) of the job has been signed by the receiver.
     *
     * @see XMLDirectSignatureJobStatus#SIGNED
     */
    SIGNED,

    /**
     * The signature job has been rejected by the receiver.
     *
     * @see XMLDirectSignatureJobStatus#REJECTED
     */
    REJECTED,

    /**
     * An error occured during the signing ceremony.
     *
     * @see XMLDirectSignatureJobStatus#FAILED
     */
    FAILED;

    public static DirectJobStatus fromXmlType(XMLDirectSignatureJobStatus xmlJobStatus) {
        switch (xmlJobStatus) {
            case SIGNED:   return SIGNED;
            case REJECTED: return REJECTED;
            case FAILED:   return FAILED;
            default:       throw new IllegalArgumentException("Unexpected status: " + xmlJobStatus);
        }
    }

}