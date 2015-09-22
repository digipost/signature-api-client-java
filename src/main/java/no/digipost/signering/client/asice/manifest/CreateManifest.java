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

import no.digipost.signering.client.asice.Schemas;
import no.digipost.signering.client.domain.Dokument;
import no.digipost.signering.client.domain.Signeringsoppdrag;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CreateManifest {

    private static final Jaxb2Marshaller marshaller;

    static {
        marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(no.digipost.signering.schema.v1.Manifest.class);
        marshaller.setSchema(Schemas.SIGNERING_MANIFEST_SCHEMA);
        try {
            marshaller.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
