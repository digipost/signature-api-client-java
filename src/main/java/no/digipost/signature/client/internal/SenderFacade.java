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

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signering.schema.v1.signature_job.XMLSignatureJobRequest;
import no.digipost.signering.schema.v1.signature_job.XMLSignatureJobResponse;
import no.digipost.signering.schema.v1.signature_job.XMLSignatureJobStatusResponse;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

public class SenderFacade {

    public static final String SIGNATURE_JOBS_PATH = "/signature-jobs";

    private final Client httpClient;
    private final WebTarget target;

    public SenderFacade(final ClientConfiguration clientConfiguration) {
        httpClient = SignatureHttpClient.create(clientConfiguration.getKeyStoreConfig());
        target = httpClient.target(clientConfiguration.getSignatureServiceRoot());
    }

    public XMLSignatureJobResponse sendSignatureJobRequest(final XMLSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        MultiPart multiPart = new MultiPart()
                .bodyPart(signatureJobBodyPart)
                .bodyPart(documentBundleBodyPart);

        return target.path(SIGNATURE_JOBS_PATH)
                .request()
                .header("Content-Type", multiPart.getMediaType())
                .post(Entity.entity(multiPart, multiPart.getMediaType()), XMLSignatureJobResponse.class);
    }

    public String tryConnecting() {
        return target.path("/")
                .request()
                .get()
                .readEntity(String.class);
    }

    public XMLSignatureJobStatusResponse sendSignatureJobStatusRequest(String statusUrl) {
        return httpClient.target(statusUrl)
                .request()
                .get()
                .readEntity(XMLSignatureJobStatusResponse.class);
    }

    public InputStream getSignedDocumentStream(String url) {
        return httpClient.target(url)
                .request()
                .get()
                .readEntity(InputStream.class);
    }
}
