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

import no.digipost.signature.client.core.XAdESReference;
import no.motif.Singular;
import no.motif.f.Predicate;

import java.util.Date;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;

public class Signature {

    private final Signer signer;
    private final SignatureStatus status;
    private final Date statusDateTime;

    private final XAdESReference xAdESReference;

    public Signature(String personalIdentificationNumber, String identifier, SignatureStatus status, Date statusDateTime, XAdESReference xAdESReference) {
        this.signer = new Signer(personalIdentificationNumber, identifier);
        this.status = status;
        this.xAdESReference = xAdESReference;
        this.statusDateTime = statusDateTime;
    }

    public String getSigner() {
        return signer.getActualIdentifier();
    }

    public SignatureStatus getStatus() {
        return status;
    }

    public boolean is(SignatureStatus status) {
        return this.status == status;
    }

    static Predicate<Signature> signatureFrom(final String signer) {
        return new Predicate<Signature>() {
            @Override
            public boolean $(Signature signature) {
                return signature.getSigner().equals(signer);
            }
        };
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
     * Exactly one of {@link Signer#personalIdentificationNumber} or {@link Signer#customIdentifier} will have a value.
     */
    private class Signer {

        final String personalIdentificationNumber;
        final String customIdentifier;

        Signer(String personalIdentificationNumber, String customIdentifier) {
            this.personalIdentificationNumber = personalIdentificationNumber;
            this.customIdentifier = customIdentifier;
        }

        String getActualIdentifier() {
            return Singular.optional(personalIdentificationNumber).orElse(customIdentifier);
        }

        @Override
        public String toString() {
            return Singular.optional(personalIdentificationNumber).map(mask).orElse(customIdentifier);
        }
    }
}
