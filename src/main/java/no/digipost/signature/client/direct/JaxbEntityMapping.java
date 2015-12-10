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

import no.digipost.signature.client.core.Sender;
import no.digipost.signering.schema.v1.common.*;
import no.digipost.signering.schema.v1.signature_job.*;

import static no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobStatus.COMPLETED;

final class JaxbEntityMapping {

    static XMLDirectSignatureJobRequest toJaxb(SignatureJob signatureJob, Sender sender) {
        return new XMLDirectSignatureJobRequest()
                .withReference(signatureJob.getReference())
                .withSigner(new XMLSigner().withPerson(new XMLPerson().withPersonalIdentificationNumber(signatureJob.getSigner().getPersonalIdentificationNumber())))
                .withSender(new XMLSender().withOrganization(sender.getOrganizationNumber()))
                .withPrimaryDocument(new XMLDocument()
                        .withTitle(new XMLTitle()
                                .withNonSensitive(signatureJob.getDocument().getSubject())
                                .withLang("NO"))
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
        if (statusResponse.getStatus() == COMPLETED) {
            XMLSuccessLinks links = statusResponse.getAdditionalInfo().getSuccessInfo().getLinks();
            return new SignatureJobStatusResponse(statusResponse.getSignatureJobId(), statusResponse.getStatus(),
                    links.getXadesUrl(), links.getPadesUrl(), links.getConfirmationUrl());
        } else {
            return new SignatureJobStatusResponse(statusResponse.getSignatureJobId(), statusResponse.getStatus());
        }
    }
}
