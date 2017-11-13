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

import no.digipost.signature.client.core.IdentifierInSignedDocuments;

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

public class SignerStatus {

    /**
     * The signer has rejected to sign the document.
     */
    public static final SignerStatus REJECTED = new SignerStatus("REJECTED");

    /**
     * The signer has not made a decision to either sign or reject the document within the
     * specified time limit.
     */
    public static final SignerStatus EXPIRED = new SignerStatus("EXPIRED");

    /**
     * The signer has yet to review the document and decide if she/he wants to sign or
     * reject it.
     */
    public static final SignerStatus WAITING = new SignerStatus("WAITING");

    /**
     * The signer has successfully signed the document.
     */
    public static final SignerStatus SIGNED = new SignerStatus("SIGNED");

    /**
     * An unexpected error occured during the signing ceremony.
     */
    public static final SignerStatus FAILED = new SignerStatus("FAILED");

    /**
     * The job has reached a state where the status of this signature is not applicable.
     * This includes the case where a signer rejects to sign, and thus ending the job in a
     * {@link DirectJobStatus#FAILED} state. Any remaining (previously {@link #WAITING})
     * signatures are marked as {@link #NOT_APPLICABLE}.
     */
    public static final SignerStatus NOT_APPLICABLE = new SignerStatus("NOT_APPLICABLE");

    /**
     * Indicates that the service was unable to retrieve the signer's name.
     * <p>
     * This happens when the signer's name is permanently unavailable in the lookup service,
     * creating and signing a new signature job with the same signer will yield the same result.
     * <p>
     * Only applicable for {@link no.digipost.signature.client.core.SignatureType#AUTHENTICATED_SIGNATURE authenticated signatures}
     * where the sender requires signed documents to contain {@link IdentifierInSignedDocuments#NAME name}
     * as {@link DirectJob.Builder#withIdentifierInSignedDocuments(IdentifierInSignedDocuments) the signer's identifier}.
     */
    public static final SignerStatus SIGNERS_NAME_NOT_AVAILABLE = new SignerStatus("SIGNERS_NAME_NOT_AVAILABLE");


    private static final List<SignerStatus> KNOWN_STATUSES = asList(
            REJECTED,
            EXPIRED,
            WAITING,
            SIGNED,
            FAILED,
            NOT_APPLICABLE,
            SIGNERS_NAME_NOT_AVAILABLE
    );

    private final String identifier;


    public SignerStatus(String identifier) {
        this.identifier = identifier;
    }

    static SignerStatus fromXmlType(String xmlSignerStatus) {
        for (SignerStatus status : KNOWN_STATUSES) {
            if (status.is(xmlSignerStatus)) {
                return status;
            }
        }

        return new SignerStatus(xmlSignerStatus);
    }

    private boolean is(String xmlSignerStatus) {
        return this.identifier.equals(xmlSignerStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SignerStatus) {
            SignerStatus that = (SignerStatus) o;
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
