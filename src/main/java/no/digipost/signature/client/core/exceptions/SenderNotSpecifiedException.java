package no.digipost.signature.client.core.exceptions;

public class SenderNotSpecifiedException extends SignatureException {

    public SenderNotSpecifiedException() {
        super("Sender is not specified. Please use ClientConfiguration.defaultSender(..) to set a default sender, " +
                "or DirectJob.Builder#withSender or PortalJob.Builder#withSender if you need to specify sender " +
                "on a per job basis (typically when acting as a broker on behalf of multiple senders).");
    }
}
