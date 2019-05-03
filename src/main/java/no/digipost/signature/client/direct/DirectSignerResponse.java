package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignerResponse;

import java.net.URI;

public class DirectSignerResponse implements WithSignerUrl {

    public static DirectSignerResponse fromJaxb(XMLDirectSignerResponse signer) {
        if (signer.getPersonalIdentificationNumber() != null) {
            return new DirectSignerResponse(signer.getPersonalIdentificationNumber(), null, URI.create(signer.getHref()), URI.create(signer.getRedirectUrl()));
        } else if (signer.getSignerIdentifier() != null) {
            return new DirectSignerResponse(null, signer.getSignerIdentifier(), URI.create(signer.getHref()), URI.create(signer.getRedirectUrl()));
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

    private DirectSignerResponse(String personalIdentificationNumber, String customIdentifier, URI signerUrl, URI redirectUrl) {
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.customIdentifier = customIdentifier;
        this.signerUrl = signerUrl;
        this.redirectUrl = redirectUrl;
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

    @Override
    public URI getSignerUrl() {
        return signerUrl;
    }

    public URI getRedirectUrl() {
        return redirectUrl;
    }

}
