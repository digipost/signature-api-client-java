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
package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signering.schema.v1.common.XMLError;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobRequest;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatusChangeRequest;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signering.schema.v1.signature_document.XMLManifest;
import no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobRequest;
import no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobResponse;
import no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobStatusResponse;
import org.etsi.uri._01903.v1_3.QualifyingProperties;
import org.etsi.uri._2918.v1_2.XAdESSignatures;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

public class Marshalling {

    private static final class Jaxb2MarshallerHolder {
        private static Jaxb2Marshaller instance; static {
            instance = new Jaxb2Marshaller();
            instance.setClassesToBeBound(XMLManifest.class, XMLDirectSignatureJobRequest.class, XMLDirectSignatureJobResponse.class, XMLDirectSignatureJobStatusResponse.class,
                    XMLPortalSignatureJobRequest.class, QualifyingProperties.class, XAdESSignatures.class, XMLPortalSignatureJobStatusChangeResponse.class,
                    XMLPortalSignatureJobStatusChangeRequest.class, XMLError.class);
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
        public static final ClassPathResource PORTAL_SIGNATURE_JOB_SCHEMA = new ClassPathResource("portal-signature-job.xsd");
        public static final ClassPathResource SIGNATURE_JOB_COMMON_SCHEMA = new ClassPathResource("common.xsd");
        public static final ClassPathResource XMLDSIG_SCHEMA = new ClassPathResource("thirdparty/xmldsig-core-schema.xsd");
        public static final ClassPathResource ASICE_SCHEMA = new ClassPathResource("thirdparty/ts_102918v010201.xsd");
        public static final ClassPathResource XADES_SCHEMA = new ClassPathResource("thirdparty/XAdES.xsd");

        public static Resource[] allSchemaResources() {
            return new Resource[]{
                    SIGNATURE_DOCUMENT_SCHEMA, SIGNATURE_JOB_SCHEMA, PORTAL_SIGNATURE_JOB_SCHEMA, SIGNATURE_JOB_COMMON_SCHEMA, XMLDSIG_SCHEMA, ASICE_SCHEMA, XADES_SCHEMA
            };
        }
    }

}
