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

import no.digipost.signature.client.core.*;
import no.posten.signering.schema.v1.XMLDocument;
import no.posten.signering.schema.v1.XMLSender;
import no.posten.signering.schema.v1.XMLSigner;
import no.posten.signering.schema.v1.XMLSigners;
import no.posten.signering.schema.v1.XMLPortalSignatureJobRequest;
import no.posten.signering.schema.v1.XMLPortalSignatureJobStatusChangeResponse;

final class JaxbEntityMapping {

    static XMLPortalSignatureJobRequest toJaxb(PortalSignatureJob job, Sender sender) {
        XMLSigners xmlSigners = new XMLSigners();
        for (Signer signer : job.getSigners()) {
            xmlSigners.getSigners().add(new XMLSigner().withPersonalIdentificationNumber(signer.getPersonalIdentificationNumber()));
        }

        return new XMLPortalSignatureJobRequest()
                .withReference(job.getReference())
                .withSigners(xmlSigners)
                .withSender(new XMLSender().withOrganization(sender.getOrganizationNumber()))
                .withPrimaryDocument(new XMLDocument()
                        .withTitle(job.getDocument().getSubject())
                        .withDescription(job.getDocument().getMessage())
                        .withHref(job.getDocument().getFileName())
                        .withMime(job.getDocument().getMimeType()));
    }


    static PortalSignatureJobStatusChanged fromJaxb(XMLPortalSignatureJobStatusChangeResponse statusChange) {
        return new PortalSignatureJobStatusChanged(
                statusChange.getSignatureJobId(), PortalSignatureJobStatus.fromXmlType(statusChange.getStatus()),
                ConfirmationReference.of(statusChange.getConfirmationUrl()),
                SignatureStatus.fromXmlType(statusChange.getSignatures().getSignature().getStatus()),
                XAdESReference.of(statusChange.getSignatures().getSignature().getXadesUrl()),
                PAdESReference.of(statusChange.getSignatures().getPadesUrl())
        );
    }
}
