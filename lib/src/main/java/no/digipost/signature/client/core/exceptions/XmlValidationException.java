package no.digipost.signature.client.core.exceptions;

public class XmlValidationException extends SignatureException {

    public XmlValidationException(String message, Exception e) {
        super(message, e);
    }
}
