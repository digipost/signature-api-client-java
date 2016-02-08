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

import no.digipost.signature.api.xml.XMLDirectSignatureJobManifest;
import no.digipost.signature.api.xml.XMLDocument;
import no.digipost.signature.api.xml.XMLSender;
import no.digipost.signature.api.xml.XMLSigner;
import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.direct.DirectJob;

public class CreateDirectManifest extends ManifestCreator<DirectJob> {

    @Override
    Object buildXmlManifest(DirectJob job, Sender sender) {
        Document document = job.getDocument();

        return new XMLDirectSignatureJobManifest()
                .withSigner(new XMLSigner().withPersonalIdentificationNumber(job.getSigner().getPersonalIdentificationNumber()))
                .withSender(new XMLSender().withOrganizationNumber(sender.getOrganizationNumber()))
                .withDocument(new XMLDocument()
                        .withTitle(document.getSubject())
                        .withDescription(document.getMessage())
                        .withHref(document.getFileName())
                        .withMime(document.getMimeType())
                );
    }
}
