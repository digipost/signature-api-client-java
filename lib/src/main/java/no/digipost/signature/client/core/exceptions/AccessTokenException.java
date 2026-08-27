package no.digipost.signature.client.core.exceptions;

/**
 * Thrown when an access token could not be acquired from the configured OAuth 2.0 token endpoint,
 * or when the token endpoint's response could not be understood.
 *
 * @see no.digipost.signature.client.security.JwtAuthConfig
 */
public class AccessTokenException extends SignatureException {

    public AccessTokenException(final String message) {
        super(message);
    }

    public AccessTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
