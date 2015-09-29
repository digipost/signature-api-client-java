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
package no.digipost.signering.client.asice.manifest;

import no.digipost.signering.client.domain.Dokument;
import no.digipost.signering.client.domain.Signeringsoppdrag;
import no.digipost.signering.client.domain.exceptions.RuntimeIOException;
import no.digipost.signering.client.domain.exceptions.XmlValideringException;
import no.digipost.signering.client.internal.Marshalling;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CreateManifest {

    private static final Jaxb2Marshaller marshaller = Marshalling.instance();

    public Manifest createManifest(final Signeringsoppdrag signeringsoppdrag) {
        Dokument dokument = signeringsoppdrag.getDokument();
        no.digipost.signering.schema.v1.Manifest manifest = new no.digipost.signering.schema.v1.Manifest()
                .withSignatar(signeringsoppdrag.getSignatar())
                .withEmne(dokument.getEmne())
                .withFilnavn(dokument.getFileName())
                .withMimeType(dokument.getMimeType());

        try (ByteArrayOutputStream manifestStream = new ByteArrayOutputStream()) {
            marshaller.marshal(manifest, new StreamResult(manifestStream));
            return new Manifest(manifestStream.toByteArray());
        } catch (MarshallingFailureException e) {
            if (e.getMostSpecificCause() instanceof SAXParseException) {
                throw new XmlValideringException("Kunne ikke validere generert Manifest XML. Sjekk at alle påkrevde input er satt og ikke er null", (SAXParseException) e.getMostSpecificCause());
            }
            throw e;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }
}
