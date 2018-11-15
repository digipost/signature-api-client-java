package no.digipost.signature.client.core.exceptions;

public class KeyException extends ConfigurationException {

    public KeyException(String message, Exception e) {
        super(message, e);
    }

    public KeyException(String s) {
        super(s);
    }

}
