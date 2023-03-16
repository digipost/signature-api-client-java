package no.digipost.signature.client.core.exceptions;

import no.digipost.signature.api.xml.XMLError;

public class BrokerNotAuthorizedException extends SignatureException {
    public BrokerNotAuthorizedException(XMLError error) {
        super(error.getErrorMessage());
    }
}
