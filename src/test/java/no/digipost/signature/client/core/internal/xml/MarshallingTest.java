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

import static no.digipost.signature.client.core.internal.xml.Marshalling.marshal;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MarshallingTest {

    @Test
    public void valid_objects_can_be_marshalled() {
        XMLSender sender = new XMLSender().withOrganizationNumber("123456789");
        XMLSigner signer = new XMLSigner().withPersonalIdentificationNumber("12345678910");
        XMLDocument document = new XMLDocument("Subject", "Message", "document.pdf", "application/pdf");
        XMLExitUrls exitUrls = new XMLExitUrls()
                .withCompletionUrl("http://localhost/signed")
                .withRejectionUrl("http://localhost/rejected")
                .withErrorUrl("http://localhost/failed");

        XMLDirectSignatureJobRequest directJob = new XMLDirectSignatureJobRequest("123abc", exitUrls);
        XMLDirectSignatureJobManifest directManifest = new XMLDirectSignatureJobManifest(signer, sender, document);

        marshal(directJob, new ByteArrayOutputStream());
        marshal(directManifest, new ByteArrayOutputStream());

        XMLPortalSignatureJobRequest portalJob = new XMLPortalSignatureJobRequest("123abc");
        XMLPortalSignatureJobManifest portalManifest = new XMLPortalSignatureJobManifest(new XMLSigners().withSigners(signer), sender, document, new XMLAvailability().withActivationTime(new Date()));
        marshal(portalJob, new ByteArrayOutputStream());
        marshal(portalManifest, new ByteArrayOutputStream());
    }

    @Test
    public void invalid_signature_job_request_causes_exceptions() {
        XMLExitUrls exitUrls = new XMLExitUrls()
                .withCompletionUrl(null)
                .withRejectionUrl("http://localhost/rejected")
                .withErrorUrl("http://localhost/failed");

        XMLDirectSignatureJobRequest signatureJobRequest = new XMLDirectSignatureJobRequest("123abc", exitUrls);

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
        XMLSigner signer = new XMLSigner().withPersonalIdentificationNumber("12345678910");
        XMLDocument document = new XMLDocument("Subject", "Message", null, "application/pdf");

        XMLDirectSignatureJobManifest directManifest = new XMLDirectSignatureJobManifest(signer, sender, document);
        XMLPortalSignatureJobManifest portalManifest = new XMLPortalSignatureJobManifest(new XMLSigners().withSigners(signer), sender, document, null);

        try {
            marshal(directManifest, new ByteArrayOutputStream());
            marshal(portalManifest, new ByteArrayOutputStream());
            fail("Should have failed with XSD-validation error due to href-attribute on document element being empty.");
        } catch (MarshallingFailureException e) {
            assertThat(e.getMessage(), allOf(containsString("href"), containsString("must appear")));
        }
    }

}
