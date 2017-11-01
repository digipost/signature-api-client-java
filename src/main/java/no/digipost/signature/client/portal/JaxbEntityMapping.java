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
import no.digipost.signature.api.xml.XMLSignature;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;

import java.util.ArrayList;
import java.util.List;

final class JaxbEntityMapping {

    static XMLPortalSignatureJobRequest toJaxb(PortalJob job) {
        return new XMLPortalSignatureJobRequest()
                .withReference(job.getReference());
    }

    static PortalJobResponse fromJaxb(XMLPortalSignatureJobResponse xmlPortalSignatureJobResponse) {
        return new PortalJobResponse(xmlPortalSignatureJobResponse.getSignatureJobId(), CancellationUrl.of(xmlPortalSignatureJobResponse.getCancellationUrl()));
    }


    static PortalJobStatusChanged fromJaxb(XMLPortalSignatureJobStatusChangeResponse statusChange) {
        List<Signature> signatures = new ArrayList<>();
        for (XMLSignature xmlSignature : statusChange.getSignatures().getSignatures()) {
            signatures.add(new Signature(
                    xmlSignature.getPersonalIdentificationNumber(),
                    xmlSignature.getIdentifier(),
                    SignatureStatus.fromXmlType(xmlSignature.getStatus()),
                    xmlSignature.getStatus().getSince().toInstant(),
                    XAdESReference.of(xmlSignature.getXadesUrl())
            ));
        }


        return new PortalJobStatusChanged(
                statusChange.getSignatureJobId(), PortalJobStatus.fromXmlType(statusChange.getStatus()),
                ConfirmationReference.of(statusChange.getConfirmationUrl()),
                CancellationUrl.of(statusChange.getCancellationUrl()),
                PAdESReference.of(statusChange.getSignatures().getPadesUrl()),
                signatures
        );
    }
}
