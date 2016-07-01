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
import no.digipost.signature.api.xml.thirdparty.asice.XAdESSignatures;
import no.digipost.signature.api.xml.thirdparty.xades.QualifyingProperties;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;

public class Marshalling {

    private static final Class[] OUTBOUND_CLASSES = new Class[]{
            XMLDirectSignatureJobManifest.class, XMLDirectSignatureJobRequest.class, XMLPortalSignatureJobManifest.class, XMLPortalSignatureJobRequest.class, QualifyingProperties.class, XAdESSignatures.class
    };
    private static final Class[] INBOUND_CLASSES = new Class[]{
            XMLDirectSignatureJobResponse.class, XMLDirectSignatureJobStatusResponse.class, XMLPortalSignatureJobResponse.class, XMLPortalSignatureJobStatusChangeResponse.class, XMLError.class
    };

    private static final class Jaxb2MarshallerHolder {
        private static final Jaxb2Marshaller marshaller;
        private static final Jaxb2Marshaller unmarshaller;

        static {
            marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(OUTBOUND_CLASSES);
            marshaller.setSchemas(Schemas.allSchemaResources());

            unmarshaller = new Jaxb2Marshaller();
            unmarshaller.setClassesToBeBound(INBOUND_CLASSES);

            try {
                marshaller.afterPropertiesSet();
                unmarshaller.afterPropertiesSet();
            } catch (Exception e) {
                throw new ConfigurationException("Kunne ikke sette opp Jaxb marshaller", e);
            }
        }
    }

    public static void marshal(Object object, OutputStream entityStream) {
        Jaxb2MarshallerHolder.marshaller.marshal(object, new StreamResult(entityStream));
    }

    public static Object unmarshal(InputStream entityStream) {
        return Jaxb2MarshallerHolder.unmarshaller.unmarshal(new StreamSource(entityStream));
    }

    public static class Schemas {
        public static final ClassPathResource DIRECT_AND_PORTAL = new ClassPathResource("direct-and-portal.xsd");
        public static final ClassPathResource XMLDSIG_SCHEMA = new ClassPathResource("thirdparty/xmldsig-core-schema.xsd");
        public static final ClassPathResource ASICE_SCHEMA = new ClassPathResource("thirdparty/ts_102918v010201.xsd");
        public static final ClassPathResource XADES_SCHEMA = new ClassPathResource("thirdparty/XAdES.xsd");

        public static Resource[] allSchemaResources() {
            return new Resource[]{
                    DIRECT_AND_PORTAL, XMLDSIG_SCHEMA, ASICE_SCHEMA, XADES_SCHEMA
            };
        }
    }

}
