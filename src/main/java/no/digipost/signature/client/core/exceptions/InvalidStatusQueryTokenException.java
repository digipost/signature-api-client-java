package no.digipost.signature.client.core.exceptions;

public class InvalidStatusQueryTokenException extends SignatureException {

    public InvalidStatusQueryTokenException(String url, String errorMessageFromServer) {
        super("The token in the url '" + url + "' was not accepted when querying for status. " + errorMessageFromServer);
    }
}
