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

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.exceptions.TooEagerPollingException;
import no.digipost.signature.client.core.exceptions.UnexpectedHttpResponseStatusException;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobRequest;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatusChangeRequest;
import no.digipost.signering.schema.v1.portal_signature_job.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signering.schema.v1.signature_job.XMLSignatureJobRequest;
import no.digipost.signering.schema.v1.signature_job.XMLSignatureJobResponse;
import no.digipost.signering.schema.v1.signature_job.XMLSignatureJobStatusResponse;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.fromStatusCode;

public class ClientHelper {

    public static final String SIGNATURE_JOBS_PATH = "/signature-jobs";
    public static final String PORTAL_SIGNATURE_JOBS_PATH = "/portal/signature-jobs";

    private final Client httpClient;
    private final WebTarget target;


    public ClientHelper(final ClientConfiguration clientConfiguration) {
        httpClient = SignatureHttpClient.create(clientConfiguration.getKeyStoreConfig());
        target = httpClient.target(clientConfiguration.getSignatureServiceRoot());
    }

    public XMLSignatureJobResponse sendSignatureJobRequest(final XMLSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        try (MultiPart multiPart = new MultiPart()) {
            multiPart
                    .bodyPart(signatureJobBodyPart)
                    .bodyPart(documentBundleBodyPart);

            return target.path(SIGNATURE_JOBS_PATH)
                    .request()
                    .header("Content-Type", multiPart.getMediaType())
                    .post(Entity.entity(multiPart, multiPart.getMediaType()), XMLSignatureJobResponse.class);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public void sendPortalSignatureJobRequest(final XMLPortalSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        try (MultiPart multiPart = new MultiPart()) {
            multiPart
                .bodyPart(signatureJobBodyPart)
                .bodyPart(documentBundleBodyPart);

            Status status = fromStatusCode(target.path(PORTAL_SIGNATURE_JOBS_PATH)
                    .request()
                    .header("Content-Type", multiPart.getMediaType())
                    .post(Entity.entity(multiPart, multiPart.getMediaType()))
                    .getStatus());

            switch (status) {
                case OK: return;
                default: throw new UnexpectedHttpResponseStatusException(status, OK);
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
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

    public XMLPortalSignatureJobStatusChangeResponse getStatusChange() {
        Response response = target.path(PORTAL_SIGNATURE_JOBS_PATH)
                .request()
                .get();
        int statusCode = response.getStatus();
        if (statusCode == NO_CONTENT.getStatusCode()) {
            return null;
        } else if (statusCode == OK.getStatusCode()) {
            return response.readEntity(XMLPortalSignatureJobStatusChangeResponse.class);
        } else if (statusCode == 429){
            throw new TooEagerPollingException(response.getHeaderString("Next-permitted-poll-time"));
        } else {
            throw new UnexpectedHttpResponseStatusException(fromStatusCode(statusCode), OK, NO_CONTENT);
        }
    }

    public void updateStatus(String url, XMLPortalSignatureJobStatusChangeRequest xmlPortalSignatureJobStatusChangeRequest) {
        httpClient.target(url)
                .request()
                .put(Entity.entity(xmlPortalSignatureJobStatusChangeRequest, APPLICATION_XML_TYPE));
    }
}
