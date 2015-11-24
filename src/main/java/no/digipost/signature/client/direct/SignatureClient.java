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
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobRequest;
import no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobResponse;
import no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobStatusResponse;

import java.io.InputStream;

import static no.digipost.signature.client.asice.CreateASiCE.createASiCE;
import static no.digipost.signature.client.direct.JaxbEntityMapping.fromJaxb;
import static no.digipost.signature.client.direct.JaxbEntityMapping.toJaxb;

public class SignatureClient {

    private final ClientConfiguration clientConfiguration;
    private final ClientHelper client;

    public SignatureClient(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        this.client = new ClientHelper(clientConfiguration);
    }

    public SignatureJobResponse create(final SignatureJob signatureJob) {
        DocumentBundle documentBundle = createASiCE(signatureJob.getDocument(), signatureJob.getSigner(), clientConfiguration.getSender(), clientConfiguration.getKeyStoreConfig());
        XMLDirectSignatureJobRequest signatureJobRequest = toJaxb(signatureJob);

        XMLDirectSignatureJobResponse xmlSignatureJobResponse = client.sendSignatureJobRequest(signatureJobRequest, documentBundle);
        return new SignatureJobResponse(xmlSignatureJobResponse.getRedirectUrl(), xmlSignatureJobResponse.getStatusUrl());
    }

    public SignatureJobStatusResponse getStatus(SignatureJobReference signatureJobReference) {
        XMLDirectSignatureJobStatusResponse xmlSignatureJobStatusResponse = client.sendSignatureJobStatusRequest(signatureJobReference.getStatusUrl());
        return fromJaxb(xmlSignatureJobStatusResponse);
    }

    public InputStream getXAdES(XAdESReference xAdESReference) {
        return client.getSignedDocumentStream(xAdESReference.getxAdESUrl());
    }

    public InputStream getPAdES(PAdESReference pAdESReference) {
        return client.getSignedDocumentStream(pAdESReference.getpAdESUrl());
    }

}
