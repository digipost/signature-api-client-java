package no.digipost.signature.client.core.exceptions;

import no.digipost.signature.api.xml.XMLError;
import no.digipost.signature.client.core.internal.http.StatusCode;

import java.util.Objects;

public class UnexpectedResponseException extends SignatureException {

    private final XMLError error;
    private final int statusCode;

    public UnexpectedResponseException(StatusCode actual) {
        this(null, actual);
    }

    public UnexpectedResponseException(Object errorEntity, StatusCode actual, StatusCode ... expected) {
        this(errorEntity, null, actual, expected);
    }

    public UnexpectedResponseException(Object errorEntity, Throwable cause, StatusCode actual, StatusCode ... expected) {
        super("Expected " + prettyprintExpectedStatuses(expected) +
              ", but got " + actual.value() +
              (errorEntity != null ? " [" + errorEntity + "]" : "") +
              (cause != null ? " - " + cause.getClass().getSimpleName() + ": '" + cause.getMessage() + "'.": ""),
              cause);
        this.error = errorEntity instanceof XMLError ? (XMLError) errorEntity : null;
        this.statusCode = actual.value();
    }

    public boolean isStatusCode(int statusCode) {
        return this.statusCode == statusCode;
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
        return error != null && Objects.equals(error.getErrorCode(), errorCode);
    }

    private static String prettyprintExpectedStatuses(StatusCode ... statuses) {
        if (statuses == null || statuses.length == 0) {
            return "status not specified";
        }
        String message = "[" + prettyprintSingleStatus(statuses[0]);
        for (int i = 1; i < statuses.length; i++) {
            message += ", " + prettyprintSingleStatus(statuses[i]);
        }
        return message + "]";
    }

    private static String prettyprintSingleStatus(StatusCode status) {
        return status.value() +"";
    }

}
