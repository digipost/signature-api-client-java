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
import no.digipost.signature.client.core.exceptions.UnexpectedResponseException;
import no.digipost.signature.api.xml.*;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.*;

public class ClientHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);

    public static final String SIGNATURE_JOBS_PATH = "/signature-jobs";
    public static final String PORTAL_SIGNATURE_JOBS_PATH = "/portal/signature-jobs";

    public static final String NEXT_PERMITTED_POLL_TIME_HEADER = "X-Next-permitted-poll-time";

    private final Client httpClient;
    private final WebTarget target;


    public ClientHelper(final ClientConfiguration clientConfiguration) {
        httpClient = SignatureHttpClient.create(clientConfiguration.getKeyStoreConfig());
        target = httpClient.target(clientConfiguration.getSignatureServiceRoot());
    }

    public XMLDirectSignatureJobResponse sendSignatureJobRequest(final XMLDirectSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        try (MultiPart multiPart = new MultiPart()) {
            multiPart
                    .bodyPart(signatureJobBodyPart)
                    .bodyPart(documentBundleBodyPart);

            Response response = target.path(SIGNATURE_JOBS_PATH)
                    .request()
                    .header(CONTENT_TYPE, multiPart.getMediaType())
                    .accept(APPLICATION_XML_TYPE)
                    .post(Entity.entity(multiPart, multiPart.getMediaType()), Response.class);
            Status status = Status.fromStatusCode(response.getStatus());
            if (status == OK) {
                return response.readEntity(XMLDirectSignatureJobResponse.class);
            } else {
                XMLError error = response.readEntity(XMLError.class);
                throw new UnexpectedResponseException(error, status, OK);
            }
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

            Response response = target.path(PORTAL_SIGNATURE_JOBS_PATH)
                    .request()
                    .header(CONTENT_TYPE, multiPart.getMediaType())
                    .accept(APPLICATION_XML_TYPE)
                    .post(Entity.entity(multiPart, multiPart.getMediaType()));
            Status status = fromStatusCode(response.getStatus());
            if (status != OK) {
                XMLError error = response.readEntity(XMLError.class);
                throw new UnexpectedResponseException(error, status, OK);
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(String statusUrl) {
        return httpClient.target(statusUrl)
                .request()
                .get()
                .readEntity(XMLDirectSignatureJobStatusResponse.class);
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
                .accept(APPLICATION_XML_TYPE)
                .get();
        int statusCode = response.getStatus();
        if (statusCode == NO_CONTENT.getStatusCode()) {
            return null;
        } else if (statusCode == OK.getStatusCode()) {
            return response.readEntity(XMLPortalSignatureJobStatusChangeResponse.class);
        } else if (statusCode == 429){
            throw new TooEagerPollingException(response.getHeaderString(NEXT_PERMITTED_POLL_TIME_HEADER));
        } else {
            XMLError error = response.readEntity(XMLError.class);
            throw new UnexpectedResponseException(error, Status.fromStatusCode(statusCode), OK, NO_CONTENT);
        }
    }

    public void confirm(Confirmable confirmable) {
        if (confirmable.getConfirmationReference() != null) {
            String url = confirmable.getConfirmationReference().getConfirmationUrl();
            LOG.info("Sends confirmation for '{}' to URL {}", confirmable, url);
            Response response = httpClient.target(url)
                    .request()
                    .accept(APPLICATION_XML_TYPE)
                    .header("Content-Length", 0)
                    .post(Entity.entity(null, APPLICATION_XML_TYPE));
            Status status = Status.fromStatusCode(response.getStatus());
            if (status != OK) {
                XMLError error;
                try {
                    error = response.readEntity(XMLError.class);
                } catch (Exception e) {
                    throw new UnexpectedResponseException(null, e, status, OK);
                }
                throw new UnexpectedResponseException(error, status, OK);
            }
        } else {
            LOG.info("Does not need to send confirmation for '{}'", confirmable);
        }
    }
}
