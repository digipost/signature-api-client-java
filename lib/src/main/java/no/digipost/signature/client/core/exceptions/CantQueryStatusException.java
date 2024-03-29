package no.digipost.signature.client.core.exceptions;

import no.digipost.signature.client.core.internal.http.StatusCode;

public class CantQueryStatusException extends SignatureException {

    public CantQueryStatusException(StatusCode status, String errorMessageFromServer) {
        super("The service refused to process the status request. This happens when the job has not been completed " +
                "(i.e. the signer haven't signed or rejected). Please wait until the signer have been redirected to " +
                "one of the exit URLs provided in the initial request before querying the job's status. The server response was " +
                status.value() + " '" + errorMessageFromServer + "'");
    }
}
