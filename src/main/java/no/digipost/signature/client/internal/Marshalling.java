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

import no.digipost.signature.client.domain.exceptions.ConfigurationException;
import no.digipost.signering.schema.v1.Manifest;
import no.digipost.signering.schema.v1.SignatureJobRequest;
import no.digipost.signering.schema.v1.SignatureJobResponse;
import org.etsi.uri._01903.v1_3.QualifyingProperties;
import org.etsi.uri._2918.v1_2.XAdESSignatures;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class Marshalling {

    private static final class Jaxb2MarshallerHolder {
        private static Jaxb2Marshaller instance; static {
            instance = new Jaxb2Marshaller();
            instance.setClassesToBeBound(Manifest.class, SignatureJobRequest.class, SignatureJobResponse.class, QualifyingProperties.class, XAdESSignatures.class);
            instance.setSchemas(Schemas.allSchemaResources());
            try {
                instance.afterPropertiesSet();
            } catch (Exception e) {
                throw new ConfigurationException("Kunne ikke sette opp Jaxb marshaller", e);
            }
        }
    }

    public static Jaxb2Marshaller instance() {
        return Jaxb2MarshallerHolder.instance;
    }

    public static class Schemas {
        public static final ClassPathResource SIGNATURE_DOCUMENT_SCHEMA = new ClassPathResource("signature-document.xsd");
        public static final ClassPathResource SIGNATURE_JOB_SCHEMA = new ClassPathResource("signature-job.xsd");
        public static final ClassPathResource XMLDSIG_SCHEMA = new ClassPathResource("thirdparty/xmldsig-core-schema.xsd");
        public static final ClassPathResource ASICE_SCHEMA = new ClassPathResource("thirdparty/ts_102918v010201.xsd");
        public static final ClassPathResource XADES_SCHEMA = new ClassPathResource("thirdparty/XAdES.xsd");

        public static Resource[] allSchemaResources() {
            return new Resource[]{
                    SIGNATURE_DOCUMENT_SCHEMA, SIGNATURE_JOB_SCHEMA, XMLDSIG_SCHEMA, ASICE_SCHEMA, XADES_SCHEMA
            };
        }
    }

    public static <T> T unmarshal(final Jaxb2Marshaller marshaller, final InputStream data, final Class<T> clazz) {
        try {
            return marshaller.getJaxbContext().createUnmarshaller().unmarshal(new StreamSource(data), clazz).getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static void marshal(final Jaxb2Marshaller marshaller, final Object data, final StreamResult result) {
        try {
            marshaller.getJaxbContext().createMarshaller().marshal(data, result);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
