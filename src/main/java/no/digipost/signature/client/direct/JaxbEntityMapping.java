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
import no.digipost.signature.api.xml.XMLSignerSpecificUrl;
import no.digipost.signature.api.xml.XMLSignerStatus;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.direct.RedirectUrls.RedirectUrl;
import no.digipost.signature.client.core.DeleteDocumentsUrl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static no.digipost.signature.client.core.internal.ActualSender.getActualSender;

final class JaxbEntityMapping {

    static XMLDirectSignatureJobRequest toJaxb(DirectJob signatureJob, Optional<Sender> globalSender) {
        Sender actualSender = getActualSender(signatureJob.getSender(), globalSender);

        return new XMLDirectSignatureJobRequest()
                .withReference(signatureJob.getReference())
                .withExitUrls(new XMLExitUrls()
                        .withCompletionUrl(signatureJob.getCompletionUrl())
                        .withRejectionUrl(signatureJob.getRejectionUrl())
                        .withErrorUrl(signatureJob.getErrorUrl())
                )
                .withStatusRetrievalMethod(signatureJob.getStatusRetrievalMethod().map(StatusRetrievalMethod::getXmlEnumValue).orElse(null))
                .withPollingQueue(actualSender.getPollingQueue().value);
    }

    static DirectJobResponse fromJaxb(XMLDirectSignatureJobResponse xmlSignatureJobResponse) {
        List<RedirectUrl> redirectUrls = xmlSignatureJobResponse.getRedirectUrls().stream()
                .map(RedirectUrl::fromJaxb)
                .collect(toList());

        return new DirectJobResponse(xmlSignatureJobResponse.getSignatureJobId(), redirectUrls, xmlSignatureJobResponse.getStatusUrl());
    }

    static DirectJobStatusResponse fromJaxb(XMLDirectSignatureJobStatusResponse statusResponse, Instant nextPermittedPollTime) {
        List<Signature> signatures = new ArrayList<>();
        for (XMLSignerStatus signerStatus : statusResponse.getStatuses()) {
            String xAdESUrl = statusResponse.getXadesUrls().stream()
                    .filter(forSigner(signerStatus.getSigner()))
                    .findFirst()
                    .map(XMLSignerSpecificUrl::getValue)
                    .orElse(null);

            signatures.add(new Signature(
                    signerStatus.getSigner(),
                    SignerStatus.fromXmlType(signerStatus.getValue()),
                    signerStatus.getSince().toInstant(),
                    XAdESReference.of(xAdESUrl)
            ));
        }

        return new DirectJobStatusResponse(
                statusResponse.getSignatureJobId(),
                DirectJobStatus.fromXmlType(statusResponse.getSignatureJobStatus()),
                ConfirmationReference.of(statusResponse.getConfirmationUrl()),
                DeleteDocumentsUrl.of(statusResponse.getDeleteDocumentsUrl()),
                signatures,
                PAdESReference.of(statusResponse.getPadesUrl()),
                nextPermittedPollTime);
    }

    private static Predicate<XMLSignerSpecificUrl> forSigner(final String signer) {
        return url -> url.getSigner().equals(signer);
    }
}
