package no.digipost.signature.client.core.exceptions;

/**
 * Thrown from API calls when an access token could not be acquired, e.g. if the token endpoint
 * rejects the client id or broker id. Tokens are acquired on first use, not when the client is built.
 */
public class AccessTokenException extends SignatureException {

    public AccessTokenException(String message) {
        this(message, null);
    }

    public AccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
