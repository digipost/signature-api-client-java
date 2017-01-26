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

import no.digipost.signature.client.core.OnBehalfOf;
import no.digipost.signature.client.core.SignatureType;
import no.digipost.signature.client.core.internal.SignerCustomizations;
import no.motif.Singular;
import no.motif.single.Optional;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;
import static no.motif.Singular.optional;

public class DirectSigner {

    public static Builder withPersonalIdentificationNumber(String personalIdentificationNumber) {
        return new Builder(personalIdentificationNumber, null);
    }

    public static Builder withCustomIdentifier(String customIdentifier) {
        return new Builder(null, customIdentifier);
    }

    public static final class Builder implements SignerCustomizations<Builder> {

        private String personalIdentificationNumber;
        private String customIdentifier;
        private Optional<SignatureType> signatureType = Singular.none();
        private Optional<OnBehalfOf> onBehalfOf = Singular.none();

        private Builder(String personalIdentificationNumber, String customIdentifier) {
            this.personalIdentificationNumber = personalIdentificationNumber;
            this.customIdentifier = customIdentifier;
        }

        @Override
        public Builder withSignatureType(SignatureType type) {
            this.signatureType = optional(type);
            return this;
        }

        @Override
        public Builder withOnBehalfOf(OnBehalfOf onBehalfOf) {
            this.onBehalfOf = optional(onBehalfOf);
            return this;
        }

        public DirectSigner build() {
            return new DirectSigner(personalIdentificationNumber, customIdentifier, signatureType, onBehalfOf);
        }

    }



    private final String personalIdentificationNumber;
    private final String customIdentifier;
    private final Optional<SignatureType> signatureType;
    private final Optional<OnBehalfOf> onBehalfOf;

    private DirectSigner(String personalIdentificationNumber, String customIdentifier, Optional<SignatureType> signatureType, Optional<OnBehalfOf> onBehalfOf) {
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.customIdentifier = customIdentifier;
        this.signatureType = signatureType;
        this.onBehalfOf = onBehalfOf;
    }

    public boolean isIdentifiedByPersonalIdentificationNumber() {
        return personalIdentificationNumber != null;
    }

    public String getPersonalIdentificationNumber() {
        if (!isIdentifiedByPersonalIdentificationNumber()) {
            throw new IllegalStateException(this + " is not identified by personal identification number, use getCustomIdentifier() instead.");
        }
        return personalIdentificationNumber;
    }

    public String getCustomIdentifier() {
        if (customIdentifier == null) {
            throw new IllegalStateException(this + " is not identified by a custom identifier, use getPersonalIdentificationNumber() instead.");
        }
        return customIdentifier;
    }

    public Optional<SignatureType> getSignatureType() {
        return signatureType;
    }

    public Optional<OnBehalfOf> getOnBehalfOf() {
        return onBehalfOf;
    }

    @Override
    public String toString() {
        return DirectSigner.class.getSimpleName() + ": " + (isIdentifiedByPersonalIdentificationNumber() ? mask(personalIdentificationNumber) : customIdentifier);
    }

}
