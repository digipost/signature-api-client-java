package no.digipost.signature.client.core.exceptions;

public class ConfigurationException extends SignatureException {

    public ConfigurationException(final String message) {
        this(message, null);
    }

    public ConfigurationException(final String message, final Exception e) {
        super(message, e);
    }
}
