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

import no.digipost.signature.api.xml.XMLNotifications;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.exceptions.SignatureException;

import java.util.Date;
import java.util.function.Predicate;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;

public class Signature {

    private final Signer signer;
    private final SignatureStatus status;
    private final Date statusDateTime;

    private final XAdESReference xAdESReference;

    public Signature(String personalIdentificationNumber, XMLNotifications identifier, SignatureStatus status, Date statusDateTime, XAdESReference xAdESReference) {
        this.signer = new Signer(personalIdentificationNumber, identifier);
        this.status = status;
        this.xAdESReference = xAdESReference;
        this.statusDateTime = statusDateTime;
    }

    /**
     * Retrieves signer's personal identification number. If signer is identified
     * by contact information, use {@link PortalJobStatusChanged#getSignatureFrom(SignerIdentifier)}.
     *
     * @throws SignatureException if signer is identified by contact information.
     */
    public String getSigner() {
        if (signer.hasPersonalIdentificationNumber()) {
            return signer.personalIdentificationNumber;
        }
        throw new SignatureException("Can't retrieve signers identified by contact information using this method. Use method PortalJobStatusChange.getSignatureFrom() instead.");
    }

    public SignatureStatus getStatus() {
        return status;
    }

    public boolean is(SignatureStatus status) {
        return this.status == status;
    }

    static Predicate<Signature> signatureFrom(final SignerIdentifier signer) {
        return signature -> signature.signer.isSameAs(signer);
    }

    /**
     * @return Point in time when the action (document was signed, signature job expired, etc.) leading to the
     * current {@link Signature#status} happened.
     */
    public Date getStatusDateTime() {
        return statusDateTime;
    }

    public XAdESReference getxAdESUrl() {
        return xAdESReference;
    }

    @Override
    public String toString() {
        return "Signature from " + signer + " with status '" + status + "' since " + statusDateTime + "" +
                (xAdESReference != null ? ". XAdES available at " + xAdESReference.getxAdESUrl() : "");
    }

    /**
     * The signer is represented either with a personal identification number, or a custom identifier
     * as specified by the sender {@link PortalSigner upon creation of the job}.
     *
     * Exactly one of {@link Signer#personalIdentificationNumber} or {@link Signer#emailAddress} and/or
     * {@link Signer#mobileNumber} will have a value.
     */
    static class Signer {

        final String personalIdentificationNumber;
        String emailAddress;
        String mobileNumber;

        Signer(String personalIdentificationNumber, XMLNotifications identifier) {
            this.personalIdentificationNumber = personalIdentificationNumber;
            if (identifier != null) {
                if (identifier.getEmail() != null) {
                    this.emailAddress = identifier.getEmail().getAddress();
                }
                if (identifier.getSms() != null) {
                    this.mobileNumber = identifier.getSms().getNumber();
                }
            }
        }


        private static boolean isEqual(Object a, Object b) {
            return (a == null && b == null) || (a != null && a.equals(b));
        }

        boolean isSameAs(SignerIdentifier other) {
            return isEqual(this.personalIdentificationNumber, other.personalIdentificationNumber) &&
                   isEqual(this.emailAddress, other.emailAddress) &&
                   isEqual(this.mobileNumber, other.mobileNumber);
        }

        boolean hasPersonalIdentificationNumber() {
            return personalIdentificationNumber != null;
        }

        @Override
        public String toString() {
            if (personalIdentificationNumber != null) {
                return mask(personalIdentificationNumber);
            } else if (emailAddress != null && mobileNumber == null) {
                return emailAddress;
            } else if (emailAddress == null && mobileNumber != null) {
                return mobileNumber;
            } else {
                return emailAddress + " and " + mobileNumber;
            }
        }
    }
}
