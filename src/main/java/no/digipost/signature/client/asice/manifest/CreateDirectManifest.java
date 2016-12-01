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

import no.digipost.signature.api.xml.XMLAuthenticationLevel;
import no.digipost.signature.api.xml.XMLDirectDocument;
import no.digipost.signature.api.xml.XMLDirectSignatureJobManifest;
import no.digipost.signature.api.xml.XMLDirectSigner;
import no.digipost.signature.api.xml.XMLSender;
import no.digipost.signature.api.xml.XMLSignatureType;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.internal.MarshallableEnum;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;

import java.util.ArrayList;
import java.util.List;

public class CreateDirectManifest extends ManifestCreator<DirectJob> {

    @Override
    Object buildXmlManifest(DirectJob job, Sender sender) {
        DirectDocument document = job.getDocument();

        List<XMLDirectSigner> signers = new ArrayList<>();
        for (DirectSigner signer : job.getSigners()) {
            XMLDirectSigner xmlSigner = new XMLDirectSigner()
                    .withSignatureType(signer.getSignatureType().map(MarshallableEnum.To.<XMLSignatureType>xmlValue()).orNull());
            if (signer.isIdentifiedByPersonalIdentificationNumber()) {
                xmlSigner.setPersonalIdentificationNumber(signer.getPersonalIdentificationNumber());
            } else {
                xmlSigner.setSignerIdentifier(signer.getCustomIdentifier());
            }
            signers.add(xmlSigner);
        }

        return new XMLDirectSignatureJobManifest()
                .withSigners(signers)
                .withRequiredAuthentication(job.getRequiredAuthentication().map(MarshallableEnum.To.<XMLAuthenticationLevel>xmlValue()).orNull())
                .withSender(new XMLSender().withOrganizationNumber(sender.getOrganizationNumber()))
                .withDocument(new XMLDirectDocument()
                        .withTitle(document.getTitle())
                        .withDescription(document.getMessage())
                        .withHref(document.getFileName())
                        .withMime(document.getMimeType())
                );
    }
}
