package no.digipost.signature.client.core.exceptions;

public class SignatureException extends RuntimeException {

    public SignatureException(Exception e) {
        this(null, e);
    }

    public SignatureException(String message) {
        this(message, null);
    }

    public SignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
