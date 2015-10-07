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
package no.digipost.signature.client.internal;

import no.digipost.signering.schema.v1.signature_document.Manifest;
import no.digipost.signering.schema.v1.signature_job.SignatureJobRequest;
import org.junit.Test;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MarshallingTest {

    private final Jaxb2Marshaller marshaller = Marshalling.instance();

    @Test
    public void valid_objects_can_be_marshalled() {
        SignatureJobRequest signatureJobRequest = new SignatureJobRequest("12345678910", "http://localhost");
        Manifest manifest = new Manifest("Subject", "application/pdf", "document.pdf");

        marshaller.marshal(signatureJobRequest, new StreamResult(new ByteArrayOutputStream()));
        marshaller.marshal(manifest, new StreamResult(new ByteArrayOutputStream()));
    }

    @Test
    public void invalid_signature_job_request_causes_exceptions() {
        SignatureJobRequest signatureJobRequest = new SignatureJobRequest("12345678910", null);

        try {
            marshaller.marshal(signatureJobRequest, new StreamResult(new ByteArrayOutputStream()));
            fail("Should have failed with XSD-validation error due to completion-url being empty.");
        } catch (MarshallingFailureException e) {
            assertThat(e.getMessage(), allOf(containsString("completion-url"), containsString("is expected")));
        }
    }

    @Test
    public void invalid_manifest_causes_exceptions() {
        Manifest manifest = new Manifest("Subject", "application/pdf", null);

        try {
            marshaller.marshal(manifest, new StreamResult(new ByteArrayOutputStream()));
            fail("Should have failed with XSD-validation error due to filename being empty.");
        } catch (MarshallingFailureException e) {
            assertThat(e.getMessage(), allOf(containsString("file-name"), containsString("is expected")));
        }
    }

}
