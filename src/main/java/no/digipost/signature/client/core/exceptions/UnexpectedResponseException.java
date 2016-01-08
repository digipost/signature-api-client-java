/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client.core.exceptions;

import no.digipost.signature.api.xml.XMLError;

import javax.ws.rs.core.Response.Status;

public class UnexpectedResponseException extends SignatureException {

    private final XMLError error;
    private final Status actualStatus;

    public UnexpectedResponseException(XMLError error, Status actual, Status ... expected) {
        this(error, null, actual, expected);
    }

    public UnexpectedResponseException(XMLError error, Throwable cause, Status actual, Status ... expected) {
        super("Expected " + prettyprintExpectedStatuses(expected) +
              ", but got " + actual.getStatusCode() + " " + actual.getReasonPhrase() +
              (cause != null ? " - " + cause.getClass().getSimpleName() + ": '" + cause.getMessage() + "'.": ""),
              cause);
        this.error = error;
        this.actualStatus = actual;
    }

    public Status getActualStatus() {
        return actualStatus;
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

    private static String prettyprintExpectedStatuses(Status... statuses) {
        String message = "[" + prettyprintSingleStatus(statuses[0]);
        for (int i = 1; i < statuses.length; i++) {
            message += ", " + prettyprintSingleStatus(statuses[i]);
        }
        return message + "]";
    }

    private static String prettyprintSingleStatus(Status status) {
        return status.getStatusCode() + " " + status.getReasonPhrase();
    }

}
