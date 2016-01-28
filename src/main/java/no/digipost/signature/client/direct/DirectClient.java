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
package no.digipost.signature.client.direct;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.api.xml.XMLDirectSignatureJobRequest;
import no.digipost.signature.api.xml.XMLDirectSignatureJobResponse;
import no.digipost.signature.api.xml.XMLDirectSignatureJobStatusResponse;

import java.io.InputStream;

import static java.util.Collections.singletonList;
import static no.digipost.signature.client.asice.CreateASiCE.createASiCE;
import static no.digipost.signature.client.direct.JaxbEntityMapping.fromJaxb;
import static no.digipost.signature.client.direct.JaxbEntityMapping.toJaxb;

public class DirectClient {

    private final ClientConfiguration clientConfiguration;
    private final ClientHelper client;

    public DirectClient(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        this.client = new ClientHelper(clientConfiguration);
    }

    public DirectJobResponse create(DirectJob signatureJob) {
        DocumentBundle documentBundle = createASiCE(signatureJob.getDocument(), singletonList(signatureJob.getSigner()), clientConfiguration.getSender(), clientConfiguration.getKeyStoreConfig());
        XMLDirectSignatureJobRequest signatureJobRequest = toJaxb(signatureJob, clientConfiguration.getSender());

        XMLDirectSignatureJobResponse xmlSignatureJobResponse = client.sendSignatureJobRequest(signatureJobRequest, documentBundle);
        return fromJaxb(xmlSignatureJobResponse);
    }


    /**
     * Get the current status for the given {@link StatusReference}, which references the status for a specific job.
     * When processing of the status is complete (e.g. retrieving {@link #getPAdES(PAdESReference) PAdES} and/or
     * {@link #getXAdES(XAdESReference) XAdES} documents for a  {@link DirectJobStatus#SIGNED signed} job),
     * the returned status must be {@link #confirm(DirectJobStatusResponse) confirmed}.
     *
     * @param statusReference the reference to the status of a specific job.
     * @return the {@link DirectJobStatusResponse} for the job referenced by the given {@link StatusReference},
     *         never {@code null}.
     */
    public DirectJobStatusResponse getStatus(StatusReference statusReference) {
        XMLDirectSignatureJobStatusResponse xmlSignatureJobStatusResponse = client.sendSignatureJobStatusRequest(statusReference.getStatusUrl());
        return fromJaxb(xmlSignatureJobStatusResponse);
    }


    /**
     * Confirms that the status retrieved from {@link #getStatus(StatusReference)} is received.
     * If the confirmed {@link DirectJobStatus} is a terminal status
     * (e.g. {@link DirectJobStatus#SIGNED signed} or {@link DirectJobStatus#CANCELLED cancelled}),
     * the Signature service may make the job's associated resources unavailable through the API when
     * receiving the confirmation. Calling this method for a response with no {@link ConfirmationReference}
     * has no effect.
     *
     * @param receivedStatusResponse the updated status retrieved from {@link #getStatus(StatusReference)}.
     */
    public void confirm(DirectJobStatusResponse receivedStatusResponse) {
        client.confirm(receivedStatusResponse);
    }

    public InputStream getXAdES(XAdESReference xAdESReference) {
        return client.getSignedDocumentStream(xAdESReference.getxAdESUrl());
    }

    public InputStream getPAdES(PAdESReference pAdESReference) {
        return client.getSignedDocumentStream(pAdESReference.getpAdESUrl());
    }


}