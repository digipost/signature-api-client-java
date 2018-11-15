package no.digipost.signature.client.core.exceptions;

public class SignatureException extends RuntimeException {

    public SignatureException(final Exception e) {
        super(e);
    }

    public SignatureException(final String message) {
        super(message);
    }

    public SignatureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
