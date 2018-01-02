package no.digipost.signature.client.docs;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.core.PollingQueue;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.direct.DirectClient;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectJobResponse;
import no.digipost.signature.client.direct.DirectJobStatus;
import no.digipost.signature.client.direct.DirectJobStatusResponse;
import no.digipost.signature.client.direct.DirectSigner;
import no.digipost.signature.client.direct.ExitUrls;
import no.digipost.signature.client.direct.Signature;
import no.digipost.signature.client.direct.SignerStatus;
import no.digipost.signature.client.direct.StatusReference;
import no.digipost.signature.client.direct.StatusRetrievalMethod;
import no.digipost.signature.client.direct.WithExitUrls;
import no.digipost.signature.client.security.KeyStoreConfig;

import java.io.InputStream;

public class DirectUsecases {

    public static void create_client_configuration() {
        InputStream keyStore = null; // Stream created from keyStore file

        KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromKeyStore(keyStore,
                "certificateAlias", "keyStorePassword", "privateKeyPassword");

        ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
                .globalSender(new Sender("123456789"))
                .build();
    }

    public static void create_and_send_signature_job() {
        ClientConfiguration clientConfiguration = null; // As initialized earlier
        DirectClient client = new DirectClient(clientConfiguration);

        byte[] documentBytes = null; // Loaded document bytes
        DirectDocument document = DirectDocument.builder("Subject", "document.pdf", documentBytes).build();

        ExitUrls exitUrls = ExitUrls.of(
                "http://sender.org/onCompletion",
                "http://sender.org/onRejection",
                "http://sender.org/onError"
        );

        DirectSigner signer = DirectSigner.withPersonalIdentificationNumber("12345678910").build();
        DirectJob directJob = DirectJob.builder(document, exitUrls, signer).build();

        DirectJobResponse directJobResponse = client.create(directJob);
    }

    public static void get_signature_job_status() {
        DirectClient client = null; // As initialized earlier
        DirectJobResponse directJobResponse = null; // As returned when creating signature job

        String statusQueryToken = "0A3BQ54C…";

        DirectJobStatusResponse directJobStatusResponse =
                client.getStatus(StatusReference.of(directJobResponse).withStatusQueryToken(statusQueryToken));
    }

    static DirectDocument document = null;
    static WithExitUrls exitUrls = null;
    static DirectSigner signer = null;

    public static void create_job_and_status_by_polling() {
        DirectClient client = null; // As initialized earlier

        DirectJob directJob = DirectJob.builder(document, exitUrls, signer)
                .retrieveStatusBy(StatusRetrievalMethod.POLLING)
                .build();

        client.create(directJob);

        DirectJobStatusResponse statusChange = client.getStatusChange();

        if (statusChange.is(DirectJobStatus.NO_CHANGES)) {
            // Queue is empty. Must wait before polling again
        } else {
            // Recieved status update, act according to status
            DirectJobStatus status = statusChange.getStatus();
        }

        client.confirm(statusChange);
    }

    public static void get_signed_documents() {
        DirectClient client = null; // As initialized earlier
        DirectJobStatusResponse directJobStatusResponse = null; // As returned when getting job status

        if (directJobStatusResponse.isPAdESAvailable()) {
            InputStream pAdESStream = client.getPAdES(directJobStatusResponse.getpAdESUrl());
        }

        for (Signature signature : directJobStatusResponse.getSignatures()) {
            if (signature.is(SignerStatus.SIGNED)) {
                InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
            }
        }
    }

    public static void confirm_processed_signature_job() {
        DirectClient client = null; // As initialized earlier
        DirectJobStatusResponse directJobStatusResponse = null; // As returned when getting job status

        client.confirm(directJobStatusResponse);
    }

    public static void specifying_queues() {
        DirectClient client = null; // As initialized earlier
        Sender sender = new Sender("000000000", PollingQueue.of("CustomPollingQueue"));

        DirectJob directJob = DirectJob.builder(document, exitUrls, signer)
                .retrieveStatusBy(StatusRetrievalMethod.POLLING).withSender(sender)
                .build();

        client.create(directJob);

        DirectJobStatusResponse statusChange = client.getStatusChange(sender);

        if (statusChange.is(DirectJobStatus.NO_CHANGES)) {
            // Queue is empty. Must wait before polling again
        } else {
            // Recieved status update, act according to status
            DirectJobStatus status = statusChange.getStatus();
        }

        client.confirm(statusChange);
    }

}
