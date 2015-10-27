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
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.client.core.internal.KeyStoreConfig;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatusChangeRequest;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatusChangeResponse;

import java.io.InputStream;

import static no.digipost.signature.client.asice.CreateASiCE.createASiCE;
import static no.digipost.signature.client.portal.JaxbEntityMapping.toJaxb;
import static no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobSenderStatus.CONFIRMED;

public class PortalClient {

    private final ClientHelper client;
    private final KeyStoreConfig keyStoreConfig;

    public PortalClient(ClientConfiguration clientConfiguration) {
        this.client = new ClientHelper(clientConfiguration);
        this.keyStoreConfig = clientConfiguration.getKeyStoreConfig();
    }

    public void create(PortalSignatureJob job) {
        client.sendPortalSignatureJobRequest(toJaxb(job), createASiCE(job.getDocument(), keyStoreConfig));
    }

    public PortalSignatureJobStatusChanged getStatusChange() {
        XMLPortalSignatureJobStatusChangeResponse statusChange = client.getStatusChange();
        if (statusChange == null) {
            return null;
        }
        return new PortalSignatureJobStatusChanged(statusChange.getStatus(), statusChange.getUuid(), statusChange.getXadesUrl(), statusChange.getPadesUrl(), statusChange.getConfirmationUrl());
    }

    public InputStream getXAdES(XAdESReference xAdESReference) {
        return client.getSignedDocumentStream(xAdESReference.getxAdESUrl());
    }

    public InputStream getPAdES(PAdESReference pAdESReference) {
        return client.getSignedDocumentStream(pAdESReference.getpAdESUrl());
    }

    public void confirm(String url) {
        client.updateStatus(url, new XMLPortalSignatureJobStatusChangeRequest(CONFIRMED));
    }
}
