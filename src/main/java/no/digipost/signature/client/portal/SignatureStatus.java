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

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

public final class SignatureStatus {

    /**
     * The signer has rejected to sign the document.
     */
    public static final SignatureStatus REJECTED = new SignatureStatus("REJECTED");

    /**
     * This signer has been cancelled by the sender, and will not be able to sign the document.
     */
    public static final SignatureStatus CANCELLED = new SignatureStatus("CANCELLED");

    /**
     * This signer is reserved from receiving documents electronically, and will not receive
     * the document for signing.
     */
    public static final SignatureStatus RESERVED = new SignatureStatus("RESERVED");

    /**
     * We were not able to locate any channels (email, SMS) for notifying the signer to sign the document.
     */
    public static final SignatureStatus CONTACT_INFORMATION_MISSING = new SignatureStatus("CONTACT_INFORMATION_MISSING");

    /**
     * The signer has not made a decision to either sign or reject the document within the
     * specified time limit,
     */
    public static final SignatureStatus EXPIRED = new SignatureStatus("EXPIRED");

    /**
     * The signer has yet to review the document and decide if she/he wants to sign or
     * reject it.
     */
    public static final SignatureStatus WAITING = new SignatureStatus("WAITING");

    /**
     * The signer has successfully signed the document.
     */
    public static final SignatureStatus SIGNED = new SignatureStatus("SIGNED");

    /**
     * The job has reached a state where the status of this signature is not applicable.
     * This includes the case where a signer rejects to sign, and thus ending the job in a
     * {@link PortalJobStatus#FAILED} state. Any remaining (previously {@link #WAITING})
     * signatures are marked as {@link #NOT_APPLICABLE}.
     */
    public static final SignatureStatus NOT_APPLICABLE = new SignatureStatus("NOT_APPLICABLE");


    private static final List<SignatureStatus> KNOWN_STATUSES = asList(
            REJECTED,
            CANCELLED,
            RESERVED,
            CONTACT_INFORMATION_MISSING,
            EXPIRED,
            WAITING,
            SIGNED,
            NOT_APPLICABLE
    );

    private final String identifier;

    public SignatureStatus(String identifier) {
        this.identifier = identifier;
    }

    public static SignatureStatus fromXmlType(String xmlSignatureStatus) {
        for (SignatureStatus status : KNOWN_STATUSES) {
            if (status.is(xmlSignatureStatus)) {
                return status;
            }
        }

        return new SignatureStatus(xmlSignatureStatus);
    }

    private boolean is(String xmlSignatureStatus) {
        return this.identifier.equals(xmlSignatureStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SignatureStatus) {
            SignatureStatus that = (SignatureStatus) o;
            return Objects.equals(identifier, that.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
