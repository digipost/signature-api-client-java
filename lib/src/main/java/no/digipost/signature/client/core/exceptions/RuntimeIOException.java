package no.digipost.signature.client.core.exceptions;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @deprecated Not in use anymore, and was not ever meant to be caught specifically (unlikely anyone is).
 *             It will be removed in a future release. The library now instead throws {@link UncheckedIOException}
 *             instead of this.
 */
@Deprecated
public class RuntimeIOException extends SignatureException {

    /**
     * @deprecated Exception type is not used anymore,
     *             see documentation on {@link RuntimeIOException}
     */
    @Deprecated
    public RuntimeIOException(IOException e) {
        super(e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
    }

}
