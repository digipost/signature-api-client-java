package no.digipost.signature.client.core.exceptions;

public class XmlValidationException extends SignatureException {

    public XmlValidationException(final String message, final Exception e) {
        super(message, e);
    }
}
