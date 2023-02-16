package no.digipost.signature.client.core.internal.http;

import org.apache.hc.core5.http.message.StatusLine;

public enum StatusCodeFamily {
    INFORMATIONAL, SUCCESSFUL, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, OTHER;

    public static StatusCodeFamily of(StatusLine apacheHttpClientStatus) {
        return of(apacheHttpClientStatus.getStatusCode());
    }

    public static StatusCodeFamily of(int statusCode) {
        switch (statusCode / 100) {
            case 1: return INFORMATIONAL;
            case 2: return SUCCESSFUL;
            case 3: return REDIRECTION;
            case 4: return CLIENT_ERROR;
            case 5: return SERVER_ERROR;
            default: return OTHER;
        }
    }
}
