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

import no.digipost.signature.client.core.XAdESReference;
import no.motif.f.Predicate;

import static no.digipost.signature.client.direct.DirectSigner.mask;


public class Signature {

    private final String signer;
    private final SignerStatus status;
    private final XAdESReference xAdESReference;

    public Signature(String signer, SignerStatus status, XAdESReference xAdESReference) {
        this.signer = signer;
        this.status = status;
        this.xAdESReference = xAdESReference;
    }

    public boolean is(SignerStatus status) {
        return this.status == status;
    }

    public String getSigner() {
        return signer;
    }

    public boolean isFrom(String personalIdentificationNumber) {
        return this.signer.equals(personalIdentificationNumber);
    }

    public SignerStatus getStatus() {
        return status;
    }

    public XAdESReference getxAdESUrl() {
        return xAdESReference;
    }

    @Override
    public String toString() {
        return "Signature from " + mask(signer) + " with status '" + status + "'" + (xAdESReference != null ? ". XAdES available at " + xAdESReference.getxAdESUrl() : "");
    }

    static Predicate<Signature> signatureFrom(final String personalIdentificationNumber) {
        return new Predicate<Signature>() {
            @Override
            public boolean $(Signature signature) {
                return signature.isFrom(personalIdentificationNumber);
            }
        };
    }


}
