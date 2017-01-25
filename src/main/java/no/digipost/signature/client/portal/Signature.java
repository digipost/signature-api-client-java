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
    private final Date happenedAt;

    public Signature(String signer, SignatureStatus status, XAdESReference xAdESReference, Date happenedAt) {
        this.signer = signer;
        this.status = status;
        this.xAdESReference = xAdESReference;
        this.happenedAt = happenedAt;
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
     * <br>
     * Returns {@code null} if nothing has happened to this signature, i.e. {@link Signature#status} is
     * {@link SignatureStatus#WAITING WAITING}
     */
    public Date getHappenedAt() {
        return happenedAt;
    }

    @Override
    public String toString() {
        return "Signature from " + mask(signer) + " with status '" + status + "'" +
                (happenedAt != null ? ". Happened at " + happenedAt : "") +
                (xAdESReference != null ? ". XAdES available at " + xAdESReference.getxAdESUrl() : "");
    }
}
