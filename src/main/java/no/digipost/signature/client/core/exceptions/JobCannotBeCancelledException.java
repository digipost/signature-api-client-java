package no.digipost.signature.client.core.exceptions;

import no.digipost.signature.api.xml.XMLError;
import no.digipost.signature.client.core.internal.http.StatusCode;

public class JobCannotBeCancelledException extends SignatureException {

    public JobCannotBeCancelledException(StatusCode status, XMLError errorEntity) {
        this(status, errorEntity.getErrorCode(), errorEntity.getErrorMessage());
    }

    public JobCannotBeCancelledException(StatusCode status, String errorCode, String errorMessageFromServer) {
        super("The service refused to process the cancellation. This happens when the job has been completed " +
              "(i.e. all signers have signed or rejected, the job has expired, etc.) since receiving last update. " +
              "Please ask the service for status changes to get the latest changes. The server response was " +
              status.value() + " '" + errorCode + ": " + errorMessageFromServer + "'");
    }

}
