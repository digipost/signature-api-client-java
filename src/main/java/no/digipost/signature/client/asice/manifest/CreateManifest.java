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

import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.Signer;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.exceptions.XmlValidationException;
import no.digipost.signature.client.core.internal.Marshalling;
import no.digipost.signering.schema.v1.*;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CreateManifest {

    private static final Jaxb2Marshaller marshaller = Marshalling.instance();

    public Manifest createManifest(final Document document, Signer signer, Sender sender) {
        XMLManifest manifest = new XMLManifest()
                .withSigners(new XMLSigners().withSigner(new XMLSigner().withPersonalIdentificationNumber(signer.getPersonalIdentificationNumber())))
                .withSender(new XMLSender().withOrganization(sender.getOrganizationNumber()))
                .withPrimaryDocument(new XMLDocument()
                        .withTitle(document.getSubject())
                        .withDescription(document.getMessage())
                        .withHref(document.getFileName())
                        .withMime(document.getMimeType())
                );

        try (ByteArrayOutputStream manifestStream = new ByteArrayOutputStream()) {
            marshaller.marshal(manifest, new StreamResult(manifestStream));
            return new Manifest(manifestStream.toByteArray());
        } catch (MarshallingFailureException e) {
            if (e.getMostSpecificCause() instanceof SAXParseException) {
                throw new XmlValidationException("Unable to validate generated Manifest XML. Verify that all required inputs are set and non-null", (SAXParseException) e.getMostSpecificCause());
            }
            throw e;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }
}
