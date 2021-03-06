package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignerResponse;

import java.net.URI;
import java.util.Objects;

public class DirectSignerResponse implements WithSignerUrl {

    public static DirectSignerResponse fromJaxb(XMLDirectSignerResponse signer) {
        if (signer.getPersonalIdentificationNumber() != null) {
            return new DirectSignerResponse(signer.getPersonalIdentificationNumber(), null, signer.getHref(), signer.getRedirectUrl());
        } else if (signer.getSignerIdentifier() != null) {
            return new DirectSignerResponse(null, signer.getSignerIdentifier(), signer.getHref(), signer.getRedirectUrl());
        } else {
            throw new IllegalStateException(
                    "Unable to convert from " + XMLDirectSignerResponse.class.getSimpleName() + " " +
                    "to " + DirectSignerResponse.class.getSimpleName() + ", because " +
                    "both personalIdentificationNumber and signerIdentifier was null");
        }
    }


    private final String personalIdentificationNumber;
    private final String customIdentifier;
    private final URI signerUrl;
    private final URI redirectUrl;

    DirectSignerResponse(String personalIdentificationNumber, String customIdentifier, URI signerUrl, URI redirectUrl) {
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.customIdentifier = customIdentifier;
        this.signerUrl = signerUrl;
        this.redirectUrl = redirectUrl;
    }

    /**
     * Check if this signer is identified by the given identifier string, either
     * as personal identification number or a custom identifier.
     *
     * @param identifier either a personal identification number or a custom identifier
     * @return {@code true} if this signer has the given identifier, {@code false} otherwise.
     */
    public boolean hasIdentifier(String identifier) {
        return Objects.equals(personalIdentificationNumber, identifier) || Objects.equals(customIdentifier, identifier);
    }

    /**
     * Check if this signer is identified by a personal identification number. If
     * this method returns {@code true}, {@link #getPersonalIdentificationNumber()} can
     * safely be called. Likewise, if it returns {@code false}, then
     * {@link #getCustomIdentifier()} can be called.
     *
     * @return {@code true} if this signer is identified by a personal identification number,
     *         {@code false} otherwise.
     */
    public boolean isIdentifiedByPersonalIdentificationNumber() {
        return personalIdentificationNumber != null;
    }

    /**
     * Get the personal identification number for this signer.
     *
     * @return the personal identification number
     * @throws IllegalStateException if this signer is <em>not</em> identified by personal identification number
     */
    public String getPersonalIdentificationNumber() {
        if (!isIdentifiedByPersonalIdentificationNumber()) {
            throw new IllegalStateException(this + " is not identified by personal identification number, use getCustomIdentifier() instead.");
        }
        return personalIdentificationNumber;
    }

    /**
     * Get the custom identifier string for this signer.
     *
     * @return the custom identifier
     * @throws IllegalStateException if this signer is <em>not</em> identified by a custom identifier string
     */
    public String getCustomIdentifier() {
        if (customIdentifier == null) {
            throw new IllegalStateException(this + " is not identified by a custom identifier, use getPersonalIdentificationNumber() instead.");
        }
        return customIdentifier;
    }

    @Override
    public URI getSignerUrl() {
        return signerUrl;
    }

    public URI getRedirectUrl() {
        return redirectUrl;
    }

}
