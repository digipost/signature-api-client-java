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

import no.digipost.signature.api.xml.*;
import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.core.exceptions.*;
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
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.*;
import static no.digipost.signature.client.core.internal.ErrorCodes.BROKER_NOT_AUTHORIZED;

public class ClientHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);

    private final String directSignatureJobsPath;
    private final String portalSignatureJobsPath;

    public static final String NEXT_PERMITTED_POLL_TIME_HEADER = "X-Next-permitted-poll-time";

    private final Client httpClient;
    private final WebTarget target;


    public ClientHelper(final ClientConfiguration clientConfiguration) {
        portalSignatureJobsPath = format("/%s/portal/signature-jobs", clientConfiguration.getSender().getOrganizationNumber());
        directSignatureJobsPath = format("/%s/direct/signature-jobs", clientConfiguration.getSender().getOrganizationNumber());

        httpClient = SignatureHttpClient.create(clientConfiguration.getKeyStoreConfig());
        target = httpClient.target(clientConfiguration.getSignatureServiceRoot());
    }

    public XMLDirectSignatureJobResponse sendSignatureJobRequest(final XMLDirectSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        return new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                .postAsMultiPart(directSignatureJobsPath, XMLDirectSignatureJobResponse.class);
    }

    public XMLPortalSignatureJobResponse sendPortalSignatureJobRequest(final XMLPortalSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        return new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                .postAsMultiPart(portalSignatureJobsPath, XMLPortalSignatureJobResponse.class);
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(String statusUrl) {
        return parseResponse(httpClient.target(statusUrl).request().get(), XMLDirectSignatureJobStatusResponse.class);
    }

    public InputStream getSignedDocumentStream(String url) {
        return parseResponse(httpClient.target(url).request().get(), InputStream.class);
    }

    public void cancel(Cancellable cancellable) {
        if (cancellable.getCancellationUrl() != null) {
            String url = cancellable.getCancellationUrl().getUrl();
            Response response = postEmptyEntity(url);
            Status status = Status.fromStatusCode(response.getStatus());
            if (status == OK) {
                return;
            } else if (status == CONFLICT) {
                throw new NotCancellableException();
            }
            throw handleGeneralError(response, status);
        } else {
            throw new NotCancellableException();
        }
    }

    public XMLPortalSignatureJobStatusChangeResponse getStatusChange() {
        Response response = target.path(portalSignatureJobsPath)
                .request()
                .accept(APPLICATION_XML_TYPE)
                .get();
        Status status = Status.fromStatusCode(response.getStatus());
        if (status == NO_CONTENT) {
            return null;
        } else if (status == OK) {
            return response.readEntity(XMLPortalSignatureJobStatusChangeResponse.class);
        } else if (response.getStatus() == 429) {
            throw new TooEagerPollingException(response.getHeaderString(NEXT_PERMITTED_POLL_TIME_HEADER));
        } else {
            throw handleGeneralError(response, status);
        }
    }

    public void confirm(Confirmable confirmable) {
        if (confirmable.getConfirmationReference() != null) {
            String url = confirmable.getConfirmationReference().getConfirmationUrl();
            LOG.info("Sends confirmation for '{}' to URL {}", confirmable, url);
            Response response = postEmptyEntity(url);
            Status status = Status.fromStatusCode(response.getStatus());
            if (status != OK) {
                throw handleGeneralError(response, status);
            }
        } else {
            LOG.info("Does not need to send confirmation for '{}'", confirmable);
        }
    }

    private class UsingBodyParts {

        private final List<BodyPart> parts;

        UsingBodyParts(BodyPart... parts) {
            this.parts = Arrays.asList(parts);
        }

        <T> T postAsMultiPart(String path, Class<T> responseType) {
            try (MultiPart multiPart = new MultiPart()) {
                for (BodyPart bodyPart : parts) {
                    multiPart.bodyPart(bodyPart);
                }

                Response response = target.path(path)
                        .request()
                        .header(CONTENT_TYPE, multiPart.getMediaType())
                        .accept(APPLICATION_XML_TYPE)
                        .post(Entity.entity(multiPart, multiPart.getMediaType()));
                return parseResponse(response, responseType);
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }
    }

    private Response postEmptyEntity(String url) {
        return httpClient.target(url)
                .request()
                .accept(APPLICATION_XML_TYPE)
                .header("Content-Length", 0)
                .post(Entity.entity(null, APPLICATION_XML_TYPE));
    }

    private <T> T parseResponse(Response response, Class<T> responseType) {
        Status status = Status.fromStatusCode(response.getStatus());
        if (status == OK) {
            return response.readEntity(responseType);
        } else {
            throw handleGeneralError(response, status);
        }
    }

    private SignatureException handleGeneralError(Response response, Status status) {
        XMLError error;
        try {
            error = response.readEntity(XMLError.class);
        } catch (Exception e) {
            return new UnexpectedResponseException(null, e, status, OK);
        }
        if (BROKER_NOT_AUTHORIZED.sameAs(error.getErrorCode())) {
            return new BrokerNotAuthorizedException(error);
        }
        return new UnexpectedResponseException(error, status, OK);
    }
}
