package no.digipost.signature.client.core.exceptions;

public class DocumentsNotDeletableException extends SignatureException {

    public DocumentsNotDeletableException() {
        super("Unable to delete documents. This is most likely because the job has not been completed. Only completed jobs can be deleted, please verify the job's status.");
    }

}
