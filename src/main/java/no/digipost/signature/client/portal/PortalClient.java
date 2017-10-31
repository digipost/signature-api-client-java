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
package no.digipost.signature.client.portal;

import no.digipost.signature.api.xml.XMLPortalSignatureJobRequest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.api.xml.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.CreateASiCE;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.asice.manifest.CreatePortalManifest;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.Cancellable;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.client.core.internal.http.SignatureHttpClientFactory;

import java.io.InputStream;
import java.util.Optional;

import static no.digipost.signature.client.portal.JaxbEntityMapping.fromJaxb;
import static no.digipost.signature.client.portal.JaxbEntityMapping.toJaxb;
import static no.digipost.signature.client.portal.PortalJobStatusChanged.NO_UPDATED_STATUS;

public class PortalClient {

    private final ClientHelper client;
    private final CreateASiCE<PortalJob> aSiCECreator;

    public PortalClient(ClientConfiguration config) {
        this.client = new ClientHelper(SignatureHttpClientFactory.create(config), config.getGlobalSender());
        this.aSiCECreator = new CreateASiCE<>(new CreatePortalManifest(config.getClock()), config);
    }


    public PortalJobResponse create(PortalJob job) {
        DocumentBundle documentBundle = aSiCECreator.createASiCE(job);
        XMLPortalSignatureJobRequest signatureJobRequest = toJaxb(job);

        XMLPortalSignatureJobResponse xmlPortalSignatureJobResponse = client.sendPortalSignatureJobRequest(signatureJobRequest, documentBundle, job.getSender());
        return fromJaxb(xmlPortalSignatureJobResponse);
    }


    /**
     * If there is a job with an updated {@link PortalJobStatus status}, the returned object contains
     * necessary information to act on the status change. The returned object can be queried using
     * {@link PortalJobStatusChanged#is(PortalJobStatus) .is(}{@link PortalJobStatus#NO_CHANGES NO_CHANGES)}
     * to determine if there has been a status change. When processing of the status change is complete, (e.g. retrieving
     * {@link #getPAdES(PAdESReference) PAdES} and/or {@link #getXAdES(XAdESReference) XAdES} documents for a
     * {@link PortalJobStatus#COMPLETED_SUCCESSFULLY completed} job where all signers have {@link SignatureStatus signed} their documents),
     * the returned status must be {@link #confirm(PortalJobStatusChanged) confirmed}.
     *
     * @return the changed status for a job, or {@link PortalJobStatusChanged#NO_UPDATED_STATUS NO_UPDATED_STATUS},
     *         never {@code null}.
     */
    public PortalJobStatusChanged getStatusChange() {
        return getStatusChange(null);
    }

    /**
     * If there is a job with an updated {@link PortalJobStatus status}, the returned object contains
     * necessary information to act on the status change. The returned object can be queried using
     * {@link PortalJobStatusChanged#is(PortalJobStatus) .is(}{@link PortalJobStatus#NO_CHANGES NO_CHANGES)}
     * to determine if there has been a status change. When processing of the status change is complete, (e.g. retrieving
     * {@link #getPAdES(PAdESReference) PAdES} and/or {@link #getXAdES(XAdESReference) XAdES} documents for a
     * {@link PortalJobStatus#COMPLETED_SUCCESSFULLY completed} job where all signers have {@link SignatureStatus signed} their documents),
     * the returned status must be {@link #confirm(PortalJobStatusChanged) confirmed}.
     *
     * @return the changed status for a job, or {@link PortalJobStatusChanged#NO_UPDATED_STATUS NO_UPDATED_STATUS},
     *         never {@code null}.
     */

    public PortalJobStatusChanged getStatusChange(Sender sender) {
        XMLPortalSignatureJobStatusChangeResponse statusChange = client.getPortalStatusChange(Optional.ofNullable(sender));
        return statusChange == null ? NO_UPDATED_STATUS : JaxbEntityMapping.fromJaxb(statusChange);
    }


    /**
     * Confirms that the status retrieved from {@link #getStatusChange()} is received and may
     * be discarded by the Signature service and not retrieved again. Calling this method on
     * a status update with no {@link ConfirmationReference} has no effect.
     *
     * @param receivedStatusChanged the updated status retrieved from {@link #getStatusChange()}.
     */
    public void confirm(PortalJobStatusChanged receivedStatusChanged) {
        client.confirm(receivedStatusChanged);
    }

    public void cancel(Cancellable cancellable) {
        client.cancel(cancellable);
    }


    public InputStream getXAdES(XAdESReference xAdESReference) {
        return client.getSignedDocumentStream(xAdESReference.getxAdESUrl());
    }


    public InputStream getPAdES(PAdESReference pAdESReference) {
        return client.getSignedDocumentStream(pAdESReference.getpAdESUrl());
    }

}
