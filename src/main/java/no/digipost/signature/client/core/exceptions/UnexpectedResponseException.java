package no.digipost.signature.client.core.exceptions;

import no.digipost.signature.api.xml.XMLError;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import java.util.Objects;

public class UnexpectedResponseException extends SignatureException {

    private final XMLError error;
    private final StatusType actualStatus;

    public UnexpectedResponseException(StatusType actual) {
        this(null, actual);
    }

    public UnexpectedResponseException(Object errorEntity, StatusType actual, StatusType ... expected) {
        this(errorEntity, null, actual, expected);
    }

    public UnexpectedResponseException(Object errorEntity, Throwable cause, StatusType actual, StatusType ... expected) {
        super("Expected " + prettyprintExpectedStatuses(expected) +
              ", but got " + actual.getStatusCode() + " " + actual.getReasonPhrase() +
              (errorEntity != null ? " [" + errorEntity + "]" : "") +
              (cause != null ? " - " + cause.getClass().getSimpleName() + ": '" + cause.getMessage() + "'.": ""),
              cause);
        this.error = errorEntity instanceof XMLError ? (XMLError) errorEntity : null;
        this.actualStatus = actual;
    }

    public StatusType getActualStatus() {
        return actualStatus;
    }

    public boolean is(Status.Family family) {
        return actualStatus != null && actualStatus.getFamily() == family;
    }

    public boolean isStatusCode(int statusCode) {
        return actualStatus != null && actualStatus.getStatusCode() == statusCode;
    }

    public boolean isStatusCodeOf(StatusType status) {
        return isStatusCode(status.getStatusCode());
    }

    public String getErrorCode() {
        return error != null ? error.getErrorCode() : null;
    }

    public String getErrorMessage() {
        return error != null ? error.getErrorMessage() : null;
    }

    public String getErrorType() {
        return error != null ? error.getErrorType() : null;
    }

    public boolean isErrorCode(String errorCode) {
        return error != null & Objects.equals(error.getErrorCode(), errorCode);
    }

    private static String prettyprintExpectedStatuses(StatusType ... statuses) {
        if (statuses == null || statuses.length == 0) {
            return "status not specified";
        }
        String message = "[" + prettyprintSingleStatus(statuses[0]);
        for (int i = 1; i < statuses.length; i++) {
            message += ", " + prettyprintSingleStatus(statuses[i]);
        }
        return message + "]";
    }

    private static String prettyprintSingleStatus(StatusType status) {
        return status.getStatusCode() + " " + status.getReasonPhrase();
    }

}
