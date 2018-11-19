package no.digipost.signature.client.core.exceptions;

import java.util.function.Supplier;

public class SenderNotSpecifiedException extends SignatureException {

    public static final Supplier<SignatureException> SENDER_NOT_SPECIFIED = SenderNotSpecifiedException::new;

    private SenderNotSpecifiedException() {
        super("Sender is not specified. Please call ClientConfiguration#sender to set it globally, " +
                "or DirectJob.Builder#withSender or PortalJob.Builder#withSender if you need to specify sender " +
                "on a per job basis (typically when acting as a broker on behalf of multiple senders).");
    }
}
