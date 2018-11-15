package no.digipost.signature.client.core.exceptions;

public class TooEagerPollingException extends RuntimeException {

    public TooEagerPollingException() {
        super("Polling for status updates was blocked because you recently retrieved a response indicating empty queue. " +
                "Next permitted poll time should be retrieved by querying 'getNextPermittedPollTime()' from the previous response. " +
                "Polling too soon after an empty queue response is a programming error.");
    }
}
