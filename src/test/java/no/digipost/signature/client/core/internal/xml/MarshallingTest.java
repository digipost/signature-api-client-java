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
package no.digipost.signature.client.core.internal.xml;

import no.digipost.signature.api.xml.*;
import org.junit.Test;
import org.springframework.oxm.MarshallingFailureException;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static no.digipost.signature.client.core.internal.xml.Marshalling.marshal;
import static no.digipost.signature.client.core.internal.xml.Marshalling.unmarshal;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MarshallingTest {

    @Test
    public void valid_objects_can_be_marshalled() {
        XMLSender sender = new XMLSender().withOrganizationNumber("123456789");
        XMLPortalSigner portalSigner = new XMLPortalSigner().withPersonalIdentificationNumber("12345678910").withNotificationsUsingLookup(new XMLNotificationsUsingLookup().withEmail(new XMLEnabled()));
        XMLDirectSigner directSigner = new XMLDirectSigner().withPersonalIdentificationNumber("12345678910");
        XMLPortalDocument portalDocument = new XMLPortalDocument("Title", "Non-sensitive title", "Message", "document.pdf", "application/pdf");
        XMLDirectDocument directDocument = new XMLDirectDocument("Title", "Message", "document.pdf", "application/pdf");
        XMLExitUrls exitUrls = new XMLExitUrls()
                .withCompletionUrl("http://localhost/signed")
                .withRejectionUrl("http://localhost/rejected")
                .withErrorUrl("http://localhost/failed");

        XMLDirectSignatureJobRequest directJob = new XMLDirectSignatureJobRequest("123abc", exitUrls, null);
        XMLDirectSignatureJobManifest directManifest = new XMLDirectSignatureJobManifest(directSigner, sender, directDocument);

        marshal(directJob, new ByteArrayOutputStream());
        marshal(directManifest, new ByteArrayOutputStream());

        XMLPortalSignatureJobRequest portalJob = new XMLPortalSignatureJobRequest("123abc");
        XMLPortalSignatureJobManifest portalManifest = new XMLPortalSignatureJobManifest(new XMLPortalSigners().withSigners(portalSigner), sender, portalDocument, new XMLAvailability().withActivationTime(new Date()));
        marshal(portalJob, new ByteArrayOutputStream());
        marshal(portalManifest, new ByteArrayOutputStream());
    }

    @Test
    public void invalid_signature_job_request_causes_exceptions() {
        XMLExitUrls exitUrls = new XMLExitUrls()
                .withCompletionUrl(null)
                .withRejectionUrl("http://localhost/rejected")
                .withErrorUrl("http://localhost/failed");

        XMLDirectSignatureJobRequest signatureJobRequest = new XMLDirectSignatureJobRequest("123abc", exitUrls, null);

        try {
            marshal(signatureJobRequest, new ByteArrayOutputStream());
            fail("Should have failed with XSD-validation error due to completion-url being empty.");
        } catch (MarshallingFailureException e) {
            assertThat(e.getMessage(), allOf(containsString("completion-url"), containsString("is expected")));
        }
    }

    @Test
    public void invalid_manifests_causes_exceptions() {
        XMLSender sender = new XMLSender().withOrganizationNumber("123456789");
        XMLPortalSigner portalSigner = new XMLPortalSigner().withPersonalIdentificationNumber("12345678910");
        XMLDirectSigner directSigner = new XMLDirectSigner().withPersonalIdentificationNumber("12345678910");
        XMLPortalDocument portalDocument = new XMLPortalDocument("Title", "Non-sensitive title", "Message", null, "application/pdf");
        XMLDirectDocument directDocument = new XMLDirectDocument("Title", "Message", null, "application/pdf");

        XMLDirectSignatureJobManifest directManifest = new XMLDirectSignatureJobManifest(directSigner, sender, directDocument);
        XMLPortalSignatureJobManifest portalManifest = new XMLPortalSignatureJobManifest(new XMLPortalSigners().withSigners(portalSigner), sender, portalDocument, null);

        try {
            marshal(directManifest, new ByteArrayOutputStream());
            marshal(portalManifest, new ByteArrayOutputStream());
            fail("Should have failed with XSD-validation error due to href-attribute on document element being empty.");
        } catch (MarshallingFailureException e) {
            assertThat(e.getMessage(), allOf(containsString("href"), containsString("must appear")));
        }
    }

    @Test
    public void ignores_unexpected_elements_in_response() {
        XMLDirectSignatureJobStatusResponse unmarshalled = (XMLDirectSignatureJobStatusResponse) unmarshal(this.getClass().getResourceAsStream("/xml/direct_signature_job_response_with_unexpected_element.xml"));

        assertEquals(1, unmarshalled.getSignatureJobId());
        assertEquals("SIGNED", unmarshalled.getStatus().name());
    }

}
