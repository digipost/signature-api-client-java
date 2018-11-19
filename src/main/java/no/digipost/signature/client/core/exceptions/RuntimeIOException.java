package no.digipost.signature.client.core.exceptions;

import java.io.IOException;

/**
 * Wrapper for IOExceptions in situations where there is no reason to assume an IOException can occur (e.g. memory representations of streams).
 */
public class RuntimeIOException extends SignatureException {

    public RuntimeIOException(IOException e) {
        super(e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
    }

}
