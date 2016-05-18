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

import javax.ws.rs.core.Response.StatusType;

public class UnexpectedResponseException extends SignatureException {

    private final XMLError error;
    private final StatusType actualStatus;

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

    public String getErrorCode() {
        return error != null ? error.getErrorCode() : null;
    }

    public String getErrorMessage() {
        return error != null ? error.getErrorMessage() : null;
    }

    public String getErrorType() {
        return error != null ? error.getErrorType() : null;
    }

    private static String prettyprintExpectedStatuses(StatusType ... statuses) {
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
