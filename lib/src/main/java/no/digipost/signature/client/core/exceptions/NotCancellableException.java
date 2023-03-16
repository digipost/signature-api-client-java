package no.digipost.signature.client.core.exceptions;

public class NotCancellableException extends SignatureException {

    public NotCancellableException() {
        super("Unable to cancel job. This is most likely because the job has been completed. Only newly created and partially completed jobs can be cancelled, please verify the job's status.");
    }

}
