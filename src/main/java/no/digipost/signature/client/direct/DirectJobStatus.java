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
     * At least one signer has not yet performed any action to the document.
     * For details about the state, see the {@link SignerStatus status} of each signer.
     *
     * @see XMLDirectSignatureJobStatus#IN_PROGRESS
     */
    IN_PROGRESS,

    /**
     * All signers have successfully signed the document.
     *
     * @see XMLDirectSignatureJobStatus#COMPLETED_SUCCESSFULLY
     */
    COMPLETED_SUCCESSFULLY,

    /**
     * All signers have performed an action to the document, but at least one have a non successful status (e.g. rejected, expired or failed).
     *
     * @see XMLDirectSignatureJobStatus#FAILED
     */
    FAILED,

    /**
     * There has not been any changes since the last received status change.
     */
    NO_CHANGES;

    public static DirectJobStatus fromXmlType(XMLDirectSignatureJobStatus xmlJobStatus) {
        switch (xmlJobStatus) {
            case IN_PROGRESS:
                return IN_PROGRESS;
            case COMPLETED_SUCCESSFULLY:
                return COMPLETED_SUCCESSFULLY;
            case FAILED:
                return FAILED;
            default:
                throw new IllegalArgumentException("Unexpected status: " + xmlJobStatus);
        }
    }

}
