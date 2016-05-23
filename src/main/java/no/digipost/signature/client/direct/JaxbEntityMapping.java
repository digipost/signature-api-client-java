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

import no.digipost.signature.api.xml.XMLDirectSignatureJobRequest;
import no.digipost.signature.api.xml.XMLDirectSignatureJobResponse;
import no.digipost.signature.api.xml.XMLDirectSignatureJobStatusResponse;
import no.digipost.signature.api.xml.XMLExitUrls;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;

final class JaxbEntityMapping {

    static XMLDirectSignatureJobRequest toJaxb(DirectJob signatureJob) {
        return new XMLDirectSignatureJobRequest()
                .withReference(signatureJob.getReference())
                .withExitUrls(new XMLExitUrls()
                        .withCompletionUrl(signatureJob.getCompletionUrl())
                        .withRejectionUrl(signatureJob.getRejectionUrl())
                        .withErrorUrl(signatureJob.getErrorUrl())
                )
                .withStatusRetrievalMethod(signatureJob.getStatusRetrievalMethod().xmlValue);
    }

    static DirectJobResponse fromJaxb(XMLDirectSignatureJobResponse xmlSignatureJobResponse) {
        return new DirectJobResponse(xmlSignatureJobResponse.getSignatureJobId(), xmlSignatureJobResponse.getRedirectUrl(), xmlSignatureJobResponse.getStatusUrl());
    }

    static DirectJobStatusResponse fromJaxb(XMLDirectSignatureJobStatusResponse statusResponse) {
        return new DirectJobStatusResponse(
                statusResponse.getSignatureJobId(), DirectJobStatus.fromXmlType(statusResponse.getStatus()),
                ConfirmationReference.of(statusResponse.getConfirmationUrl()),
                XAdESReference.of(statusResponse.getXadesUrl()),
                PAdESReference.of(statusResponse.getPadesUrl()));
    }
}
