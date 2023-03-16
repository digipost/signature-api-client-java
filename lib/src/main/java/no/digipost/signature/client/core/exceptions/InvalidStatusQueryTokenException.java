package no.digipost.signature.client.core.exceptions;

import java.net.URI;

public class InvalidStatusQueryTokenException extends SignatureException {

    public InvalidStatusQueryTokenException(URI url, String errorMessageFromServer) {
        super("The token in the url '" + url + "' was not accepted when querying for status. " + errorMessageFromServer);
    }
}
