package no.digipost.signature.client.docs;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.core.PollingQueue;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.portal.Notifications;
import no.digipost.signature.client.portal.NotificationsUsingLookup;
import no.digipost.signature.client.portal.PortalClient;
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalJobResponse;
import no.digipost.signature.client.portal.PortalJobStatus;
import no.digipost.signature.client.portal.PortalJobStatusChanged;
import no.digipost.signature.client.portal.PortalSigner;
import no.digipost.signature.client.portal.Signature;
import no.digipost.signature.client.portal.SignatureStatus;
import no.digipost.signature.client.portal.SignerIdentifier;

import java.io.InputStream;
import java.time.Instant;

import static java.util.Arrays.asList;

@SuppressWarnings({"unused", "ConstantConditions", "StatementWithEmptyBody", "null"})
class PortalClientUseCases {

    static void create_and_send_signature_job() {
        ClientConfiguration clientConfiguration = null; // As initialized earlier
        PortalClient client = new PortalClient(clientConfiguration);

        byte[] documentBytes = null; // Loaded document bytes
        PortalDocument document = PortalDocument.builder("Document title", documentBytes).build();

        PortalJob portalJob = PortalJob.builder(
                "Job title",
                asList(document),
                asList(
                    PortalSigner.identifiedByPersonalIdentificationNumber("12345678910",
                            NotificationsUsingLookup.EMAIL_ONLY).build(),
                    PortalSigner.identifiedByPersonalIdentificationNumber("12345678911",
                            Notifications.builder().withEmailTo("email@example.com").build()).build(),
                    PortalSigner.identifiedByEmail("email@example.com").build())
        ).build();

        PortalJobResponse portalJobResponse = client.create(portalJob);
    }

    static void get_status_changes() {
        PortalClient client = null; // As initialized earlier

        PortalJobStatusChanged statusChange = client.getStatusChange();

        if (statusChange.is(PortalJobStatus.NO_CHANGES)) {
            // Queue is empty. Must wait before polling again
            Instant nextPermittedPollTime = statusChange.getNextPermittedPollTime();
        } else {
            // Received status update, act according to status
            PortalJobStatus signatureJobStatus = statusChange.getStatus();
            Instant nextPermittedPollTime = statusChange.getNextPermittedPollTime();
        }

        //Get status for signer
        Signature signature = statusChange.getSignatureFrom(
                SignerIdentifier.identifiedByPersonalIdentificationNumber("12345678910")
        );

        //Confirm the receipt to remove it from the queue
        client.confirm(statusChange);
    }

    static void get_signed_documents() {
        PortalClient client = null; // As initialized earlier
        PortalJobStatusChanged statusChange = null; // As returned when polling for status changes

        // Retrieve PAdES:
        if (statusChange.isPAdESAvailable()) {
            InputStream pAdESStream = client.getPAdES(statusChange.getpAdESUrl());
        }

        // Retrieve XAdES for all signers:
        for (Signature signature : statusChange.getSignatures()) {
            if (signature.is(SignatureStatus.SIGNED)) {
                InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
            }
        }

        // … or for one specific signer:
        Signature signature = statusChange.getSignatureFrom(
                SignerIdentifier.identifiedByPersonalIdentificationNumber("12345678910"));
        if (signature.is(SignatureStatus.SIGNED)) {
            InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
        }
    }

    static void specifying_queues() {
        ClientConfiguration clientConfiguration = null; // As initialized earlier
        PortalClient client = new PortalClient(clientConfiguration);

        Sender sender = new Sender("000000000", PollingQueue.of("CustomPollingQueue"));

        byte[] documentBytes = null; // Loaded document bytes
        PortalDocument document = PortalDocument.builder("Document title", documentBytes).build();

        PortalJob portalJob = PortalJob.builder(
                "Job title",
                asList(document),
                asList(
                    PortalSigner.identifiedByPersonalIdentificationNumber("12345678910",
                            NotificationsUsingLookup.EMAIL_ONLY).build(),
                    PortalSigner.identifiedByPersonalIdentificationNumber("12345678911",
                            Notifications.builder().withEmailTo("email@example.com").build()).build(),
                    PortalSigner.identifiedByEmail("email@example.com").build())
        ).withSender(sender).build();

        PortalJobResponse portalJobResponse = client.create(portalJob);

        PortalJobStatusChanged statusChange = client.getStatusChange(sender);
    }

    static void delete_documents() {
        PortalClient client = null; // As initialized earlier
        PortalJobStatusChanged statusChange = null; // As returned when polling for status changes

        client.deleteDocuments(statusChange.getDeleteDocumentsUrl());
    }
}
