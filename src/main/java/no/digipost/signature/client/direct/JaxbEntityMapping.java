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

import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signering.schema.v1.XMLDocument;
import no.digipost.signering.schema.v1.XMLSender;
import no.digipost.signering.schema.v1.XMLSigner;
import no.digipost.signering.schema.v1.XMLDirectSignatureJobRequest;
import no.digipost.signering.schema.v1.XMLDirectSignatureJobResponse;
import no.digipost.signering.schema.v1.XMLDirectSignatureJobStatusResponse;
import no.digipost.signering.schema.v1.XMLExitUrls;

final class JaxbEntityMapping {

    static XMLDirectSignatureJobRequest toJaxb(SignatureJob signatureJob, Sender sender) {
        return new XMLDirectSignatureJobRequest()
                .withReference(signatureJob.getReference())
                .withSigner(new XMLSigner().withPersonalIdentificationNumber(signatureJob.getSigner().getPersonalIdentificationNumber()))
                .withSender(new XMLSender().withOrganization(sender.getOrganizationNumber()))
                .withPrimaryDocument(new XMLDocument()
                        .withTitle(signatureJob.getDocument().getSubject())
                        .withDescription(signatureJob.getDocument().getMessage())
                        .withHref(signatureJob.getDocument().getFileName())
                        .withMime(signatureJob.getDocument().getMimeType()))
                .withExitUrls(new XMLExitUrls()
                        .withCompletionUrl(signatureJob.getCompletionUrl())
                        .withCancellationUrl(signatureJob.getCancellationUrl())
                        .withErrorUrl(signatureJob.getErrorUrl())
                );
    }

    static SignatureJobResponse fromJaxb(XMLDirectSignatureJobResponse xmlSignatureJobResponse) {
        return new SignatureJobResponse(xmlSignatureJobResponse.getSignatureJobId(), xmlSignatureJobResponse.getRedirectUrl(), xmlSignatureJobResponse.getStatusUrl());
    }

    static SignatureJobStatusResponse fromJaxb(XMLDirectSignatureJobStatusResponse statusResponse) {
        return new SignatureJobStatusResponse(
                statusResponse.getSignatureJobId(), SignatureJobStatus.fromXmlType(statusResponse.getStatus()),
                ConfirmationReference.of(statusResponse.getConfirmationUrl()),
                XAdESReference.of(statusResponse.getXadesUrl()),
                PAdESReference.of(statusResponse.getPadesUrl()));
    }
}
