package no.digipost.signature.client.core.exceptions;

import no.digipost.signature.api.xml.XMLError;

import jakarta.ws.rs.core.Response.StatusType;

public class JobCannotBeCancelledException extends SignatureException {

    public JobCannotBeCancelledException(StatusType status, XMLError errorEntity) {
        this(status, errorEntity.getErrorCode(), errorEntity.getErrorMessage());
    }

    public JobCannotBeCancelledException(StatusType status, String errorCode, String errorMessageFromServer) {
        super("The service refused to process the cancellation. This happens when the job has been completed " +
              "(i.e. all signers have signed or rejected, the job has expired, etc.) since receiving last update. " +
              "Please ask the service for status changes to get the latest changes. The server response was " +
              status.getStatusCode() + " " + status.getReasonPhrase() + " '" + errorCode + ": " + errorMessageFromServer + "'");
    }

}
