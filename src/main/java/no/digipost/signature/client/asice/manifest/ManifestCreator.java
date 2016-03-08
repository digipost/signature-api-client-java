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

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.exceptions.XmlValidationException;
import no.digipost.signature.client.core.internal.xml.Marshalling;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class ManifestCreator<JOB extends SignatureJob> {

    private static final Jaxb2Marshaller marshaller = Marshalling.instance();

    public Manifest createManifest(JOB job, Sender sender) {
        Object xmlManifest = buildXmlManifest(job, sender);

        try (ByteArrayOutputStream manifestStream = new ByteArrayOutputStream()) {
            marshaller.marshal(xmlManifest, new StreamResult(manifestStream));
            return new Manifest(manifestStream.toByteArray());
        } catch (MarshallingFailureException e) {
            if (e.getMostSpecificCause() instanceof SAXParseException) {
                throw new XmlValidationException("Unable to validate generated Manifest XML. " +
                        "This typically happens if one or more values are not in accordance with the XSD. " +
                        "You may inspect the cause (by calling getCause()) to see which constraint has been violated.", (SAXParseException) e.getMostSpecificCause());
            }
            throw e;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    abstract Object buildXmlManifest(JOB job, Sender sender);


}
