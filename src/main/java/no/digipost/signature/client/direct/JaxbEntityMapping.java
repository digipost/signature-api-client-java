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
import no.digipost.signature.api.xml.XMLStatusRetrievalMethod;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.MarshallableEnum;
import no.digipost.signature.client.direct.RedirectUrls.RedirectUrl;
import no.motif.f.Fn;
import no.motif.f.Predicate;

import java.util.ArrayList;
import java.util.List;

import static no.motif.Iterate.on;

final class JaxbEntityMapping {

    static XMLDirectSignatureJobRequest toJaxb(DirectJob signatureJob) {
        return new XMLDirectSignatureJobRequest()
                .withReference(signatureJob.getReference())
                .withExitUrls(new XMLExitUrls()
                        .withCompletionUrl(signatureJob.getCompletionUrl())
                        .withRejectionUrl(signatureJob.getRejectionUrl())
                        .withErrorUrl(signatureJob.getErrorUrl())
                )
                .withStatusRetrievalMethod(signatureJob.getStatusRetrievalMethod().map(MarshallableEnum.To.<XMLStatusRetrievalMethod>xmlValue()).orNull());
    }

    static DirectJobResponse fromJaxb(XMLDirectSignatureJobResponse xmlSignatureJobResponse) {
        List<RedirectUrl> redirectUrls = new ArrayList<>();
        for (XMLSignerSpecificUrl redirectUrl : xmlSignatureJobResponse.getRedirectUrls()) {
            redirectUrls.add(new RedirectUrl(redirectUrl.getSigner(), redirectUrl.getValue()));
        }

        return new DirectJobResponse(xmlSignatureJobResponse.getSignatureJobId(), redirectUrls, xmlSignatureJobResponse.getStatusUrl());
    }

    static DirectJobStatusResponse fromJaxb(XMLDirectSignatureJobStatusResponse statusResponse) {
        List<Signature> signatures = new ArrayList<>();
        for (XMLSignerStatus signerStatus : statusResponse.getStatuses()) {
            String xAdESUrl = on(statusResponse.getXadesUrls())
                    .filter(forSigner(signerStatus.getSigner()))
                    .head()
                    .map(getUrl())
                    .orNull();

            signatures.add(new Signature(
                    signerStatus.getSigner(),
                    SignerStatus.fromXmlType(signerStatus.getValue()),
                    XAdESReference.of(xAdESUrl),
                    signerStatus.getSince()
            ));
        }

        return new DirectJobStatusResponse(
                statusResponse.getSignatureJobId(),
                DirectJobStatus.fromXmlType(statusResponse.getSignatureJobStatus()),
                ConfirmationReference.of(statusResponse.getConfirmationUrl()),
                signatures,
                PAdESReference.of(statusResponse.getPadesUrl()));
    }

    private static Fn<XMLSignerSpecificUrl, String> getUrl() {
        return new Fn<XMLSignerSpecificUrl, String>() {
            @Override public String $(XMLSignerSpecificUrl url) { return url.getValue(); }
        };
    }

    private static Predicate<XMLSignerSpecificUrl> forSigner(final String signer) {
        return new Predicate<XMLSignerSpecificUrl>() {
            @Override public boolean $(XMLSignerSpecificUrl url) { return url.getSigner().equals(signer); }
        };
    }
}
