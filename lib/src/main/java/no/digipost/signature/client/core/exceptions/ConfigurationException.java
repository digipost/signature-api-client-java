package no.digipost.signature.client.core.exceptions;

public class ConfigurationException extends SignatureException {

    public ConfigurationException(String message) {
        this(message, null);
    }

    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }
}
