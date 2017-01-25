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

import java.util.Date;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;

public class Signature {

    private final String signer;
    private final SignatureStatus status;
    private final XAdESReference xAdESReference;
    private final Date dateTimeForStatus;

    public Signature(String signer, SignatureStatus status, XAdESReference xAdESReference, Date dateTimeForStatus) {
        this.signer = signer;
        this.status = status;
        this.xAdESReference = xAdESReference;
        this.dateTimeForStatus = dateTimeForStatus;
    }

    public boolean is(SignatureStatus status) {
        return this.status == status;
    }

    public String getSigner() {
        return signer;
    }

    public SignatureStatus getStatus() {
        return status;
    }

    public XAdESReference getxAdESUrl() {
        return xAdESReference;
    }

    /**
     * @return Point in time when the action (document was signed, signature job expired, etc.) leading to the
     * current {@link Signature#status} happened.
     */
    public Date getDateTimeForStatus() {
        return dateTimeForStatus;
    }

    @Override
    public String toString() {
        return "Signature from " + mask(signer) + " with status '" + status + "' since " + dateTimeForStatus + "" +
                (xAdESReference != null ? ". XAdES available at " + xAdESReference.getxAdESUrl() : "");
    }
}
