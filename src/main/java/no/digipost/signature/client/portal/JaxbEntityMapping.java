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

import no.digipost.signering.schema.v1.common.XMLPerson;
import no.digipost.signering.schema.v1.common.XMLSigner;
import no.digipost.signering.schema.v1.common.XMLSigners;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobRequest;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signering.schema.v1.portal_signature_job.XMLSuccessLinks;

final class JaxbEntityMapping {

    static XMLPortalSignatureJobRequest toJaxb(PortalSignatureJob job) {
        return new XMLPortalSignatureJobRequest()
                .withId(job.getId())
                .withSigners(new XMLSigners().withSigner(new XMLSigner().withPerson(new XMLPerson().withPersonalIdentificationNumber(job.getSigner().getPersonalIdentificationNumber()))));
    }


    static PortalSignatureJobStatusChanged fromJaxb(XMLPortalSignatureJobStatusChangeResponse statusChange) {
        XMLSuccessLinks links = statusChange.getAdditionalInfo().getSuccessInfo().getLinks();
        return new PortalSignatureJobStatusChanged(statusChange.getStatus(), statusChange.getId(),
                links.getXadesUrl(), links.getPadesUrl(), links.getConfirmationUrl());
    }
}
