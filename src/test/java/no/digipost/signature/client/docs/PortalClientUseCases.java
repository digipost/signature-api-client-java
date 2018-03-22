/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import no.digipost.signature.client.security.KeyStoreConfig;

import java.io.InputStream;
import java.time.Instant;

@SuppressWarnings({"unused", "ConstantConditions", "StatementWithEmptyBody"})
class PortalClientUseCases {

    static void create_client_configuration(){
        InputStream keyStore = null; // Stream created from keyStore file

        KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromKeyStore(keyStore,
                "certificateAlias", "keyStorePassword", "privateKeyPassword");

        ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
                .globalSender(new Sender("123456789"))
                .build();
    }

    static void create_and_send_signature_job(){
        ClientConfiguration clientConfiguration = null; // As initialized earlier
        PortalClient client = new PortalClient(clientConfiguration);

        byte[] documentBytes = null; // Loaded document bytes
        PortalDocument document = PortalDocument.builder("Subject", "document.pdf", documentBytes).build();

        PortalJob portalJob = PortalJob.builder(
                document,
                PortalSigner.identifiedByPersonalIdentificationNumber("12345678910",
                        NotificationsUsingLookup.EMAIL_ONLY).build(),
                PortalSigner.identifiedByPersonalIdentificationNumber("12345678911",
                        Notifications.builder().withEmailTo("email@example.com").build()).build(),
                PortalSigner.identifiedByEmail("email@example.com").build()
        ).build();

        PortalJobResponse portalJobResponse = client.create(portalJob);
    }

    static void get_status_changes(){
        PortalClient client = null; // As initialized earlier

        PortalJobStatusChanged statusChange = client.getStatusChange();

        if (statusChange.is(PortalJobStatus.NO_CHANGES)) {
            // Queue is empty. Must wait before polling again
            Instant nextPermittedPollTime = statusChange.getNextPermittedPollTime();
        } else {
            // Recieved status update, act according to status
            PortalJobStatus signatureJobStatus = statusChange.getStatus();
            Instant nextPermittedPollTime = statusChange.getNextPermittedPollTime();
        }

    }

    static void get_signer_status(){
        PortalJobStatusChanged statusChange = null; // As returned when polling for status changes

        Signature signature = statusChange.getSignatureFrom(
                SignerIdentifier.identifiedByPersonalIdentificationNumber("12345678910")
        );
    }

    static void get_signed_documents(){
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

    static void confirm_processed_signature_job(){
        PortalClient client = null; // As initialized earlier
        PortalJobStatusChanged statusChange = null; // As returned when polling for status changes

        client.confirm(statusChange);
    }

    static void specifying_queues() {
        ClientConfiguration clientConfiguration = null; // As initialized earlier
        PortalClient client = new PortalClient(clientConfiguration);

        Sender sender = new Sender("000000000", PollingQueue.of("CustomPollingQueue"));

        byte[] documentBytes = null; // Loaded document bytes
        PortalDocument document = PortalDocument.builder("Subject", "document.pdf", documentBytes).build();

        PortalJob portalJob = PortalJob.builder(
                document,
                PortalSigner.identifiedByPersonalIdentificationNumber("12345678910",
                        NotificationsUsingLookup.EMAIL_ONLY).build(),
                PortalSigner.identifiedByPersonalIdentificationNumber("12345678911",
                        Notifications.builder().withEmailTo("email@example.com").build()).build(),
                PortalSigner.identifiedByEmail("email@example.com").build()
        ).withSender(sender).build();

        PortalJobResponse portalJobResponse = client.create(portalJob);

        PortalJobStatusChanged statusChange = client.getStatusChange(sender);
    }

}
