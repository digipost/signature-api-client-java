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

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.posten.signering.schema.v1.XMLPortalSignatureJobStatusChangeResponse;

import java.io.InputStream;

import static no.digipost.signature.client.asice.CreateASiCE.createASiCE;
import static no.digipost.signature.client.portal.JaxbEntityMapping.toJaxb;
import static no.digipost.signature.client.portal.PortalSignatureJobStatusChanged.NO_UPDATED_STATUS;

public class PortalClient {

    private final ClientHelper client;
    private final ClientConfiguration clientConfiguration;

    public PortalClient(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        this.client = new ClientHelper(clientConfiguration);
    }


    public void create(PortalSignatureJob job) {
        client.sendPortalSignatureJobRequest(toJaxb(job, clientConfiguration.getSender()), createASiCE(job.getDocument(), job.getSigner(), clientConfiguration.getSender(), clientConfiguration.getKeyStoreConfig()));
    }


    /**
     * If there is a job with an updated {@link PortalSignatureJobStatus status}, the returned object contains
     * necessary information to act on the status change. The returned object can be queried using
     * {@link PortalSignatureJobStatusChanged#is(PortalSignatureJobStatus) .is(}{@link PortalSignatureJobStatus#NO_CHANGES NO_CHANGES)}
     * to determine if there has been a status change. When processing of the status change is complete, (e.g. retrieving
     * {@link #getPAdES(PAdESReference) PAdES} and/or {@link #getXAdES(XAdESReference) XAdES} documents for a
     * {@link PortalSignatureJobStatus#COMPLETED completed} job where all signers have {@link SignatureStatus signed} their documents),
     * the returned status must be {@link #confirm(PortalSignatureJobStatusChanged) confirmed}.
     *
     * @return the changed status for a job, or {@link PortalSignatureJobStatusChanged#NO_UPDATED_STATUS NO_UPDATED_STATUS},
     *         never {@code null}.
     */
    public PortalSignatureJobStatusChanged getStatusChange() {
        XMLPortalSignatureJobStatusChangeResponse statusChange = client.getStatusChange();
        return statusChange == null ? NO_UPDATED_STATUS : JaxbEntityMapping.fromJaxb(statusChange);
    }


    /**
     * Confirms that the status retrieved from {@link #getStatusChange()} is received and may
     * be discarded by the Signature service and not retrieved again. Calling this method on
     * a status update with no {@link ConfirmationReference} has no effect.
     *
     * @param receivedStatusChanged the updated status retrieved from {@link #getStatusChange()}.
     */
    public void confirm(PortalSignatureJobStatusChanged receivedStatusChanged) {
        client.confirm(receivedStatusChanged);
    }


    public InputStream getXAdES(XAdESReference xAdESReference) {
        return client.getSignedDocumentStream(xAdESReference.getxAdESUrl());
    }


    public InputStream getPAdES(PAdESReference pAdESReference) {
        return client.getSignedDocumentStream(pAdESReference.getpAdESUrl());
    }

}
