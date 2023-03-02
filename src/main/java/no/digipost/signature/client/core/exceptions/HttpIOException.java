package no.digipost.signature.client.core.exceptions;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;

import java.io.IOException;
import java.io.UncheckedIOException;

public class HttpIOException extends UncheckedIOException {

    public HttpIOException(HttpResponse response, IOException cause) {
        this(
                "Error processing " + response + ", because " +
                cause.getClass().getSimpleName() + " '" + cause.getMessage() + "'",
                cause);
    }

    public HttpIOException(HttpRequest request, IOException cause) {
        this(
                "Error executing " + request + ", because " +
                cause.getClass().getSimpleName() + " '" + cause.getMessage() + "'",
                cause);
    }

    public HttpIOException(String message, IOException cause) {
        super(message, cause);
    }

}
