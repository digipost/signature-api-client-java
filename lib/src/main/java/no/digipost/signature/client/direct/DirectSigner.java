package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.OnBehalfOf;
import no.digipost.signature.client.core.SignatureType;
import no.digipost.signature.client.core.internal.SignerCustomizations;

import java.util.Optional;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;

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
        private Optional<SignatureType> signatureType = Optional.empty();
        private Optional<OnBehalfOf> onBehalfOf = Optional.empty();

        private Builder(String personalIdentificationNumber, String customIdentifier) {
            this.personalIdentificationNumber = personalIdentificationNumber;
            this.customIdentifier = customIdentifier;
        }

        @Override
        public Builder withSignatureType(SignatureType type) {
            this.signatureType = Optional.of(type);
            return this;
        }

        @Override
        public Builder onBehalfOf(OnBehalfOf onBehalfOf) {
            this.onBehalfOf = Optional.of(onBehalfOf);
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
