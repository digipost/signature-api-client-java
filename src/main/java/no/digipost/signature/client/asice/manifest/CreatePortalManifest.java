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
package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.api.xml.*;
import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;

public class CreatePortalManifest extends ManifestCreator<PortalJob> {

    @Override
    Object buildXmlManifest(PortalJob job, Sender sender) {
        XMLPortalSigners xmlSigners = new XMLPortalSigners();
        for (PortalSigner signer : job.getSigners()) {
            xmlSigners.getSigners().add(new XMLPortalSigner()
                    .withPersonalIdentificationNumber(signer.getPersonalIdentificationNumber())
                    .withOrder(signer.getOrder()));
        }

        Document document = job.getDocument();
        return new XMLPortalSignatureJobManifest()
                .withSigners(xmlSigners)
                .withSender(new XMLSender().withOrganizationNumber(sender.getOrganizationNumber()))
                .withDocument(new XMLDocument()
                        .withTitle(document.getSubject())
                        .withDescription(document.getMessage())
                        .withHref(document.getFileName())
                        .withMime(document.getMimeType())
                )
                .withAvailability(new XMLAvailability()
                        .withActivationTime(job.getActivationTime())
                        .withAvailableSeconds(job.getAvailableSeconds())
                );
    }
}
