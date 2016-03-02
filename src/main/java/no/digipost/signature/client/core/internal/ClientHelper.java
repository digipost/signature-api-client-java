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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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
import static no.digipost.signature.client.core.internal.ErrorCodes.SIGNING_CEREMONY_NOT_COMPLETED;

public class ClientHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);

    private final String directSignatureJobsPath;
    private final String portalSignatureJobsPath;

    public static final String NEXT_PERMITTED_POLL_TIME_HEADER = "X-Next-permitted-poll-time";

    private final Client httpClient;
    private final WebTarget target;
    private final ClientExceptionMapper clientExceptionMapper;


    public ClientHelper(final ClientConfiguration clientConfiguration) {
        portalSignatureJobsPath = format("/%s/portal/signature-jobs", clientConfiguration.getSender().getOrganizationNumber());
        directSignatureJobsPath = format("/%s/direct/signature-jobs", clientConfiguration.getSender().getOrganizationNumber());

        httpClient = SignatureHttpClient.create(clientConfiguration);
        target = httpClient.target(clientConfiguration.getSignatureServiceRoot());
        clientExceptionMapper = new ClientExceptionMapper();
    }

    public XMLDirectSignatureJobResponse sendSignatureJobRequest(final XMLDirectSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        final BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        final BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        return call(new Producer<XMLDirectSignatureJobResponse>() {
            @Override
            XMLDirectSignatureJobResponse call() {
                return new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                        .postAsMultiPart(directSignatureJobsPath, XMLDirectSignatureJobResponse.class);
            }
        });
    }

    public XMLPortalSignatureJobResponse sendPortalSignatureJobRequest(final XMLPortalSignatureJobRequest signatureJobRequest, final DocumentBundle documentBundle) {
        final BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        final BodyPart documentBundleBodyPart = new BodyPart(new ByteArrayInputStream(documentBundle.getBytes()), APPLICATION_OCTET_STREAM_TYPE);

        return call(new Producer<XMLPortalSignatureJobResponse>() {
            @Override
            XMLPortalSignatureJobResponse call() {
                return new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                        .postAsMultiPart(portalSignatureJobsPath, XMLPortalSignatureJobResponse.class);
            }
        });
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(final String statusUrl) {
        return call(new Producer<XMLDirectSignatureJobStatusResponse>() {
            @Override
            XMLDirectSignatureJobStatusResponse call() {
                Response response = httpClient.target(statusUrl)
                        .request()
                        .accept(APPLICATION_XML_TYPE)
                        .get();
                Status status = Status.fromStatusCode(response.getStatus());
                if (status == OK) {
                    return response.readEntity(XMLDirectSignatureJobStatusResponse.class);
                } else if (status == NOT_FOUND) {
                    XMLError error = extractError(response);
                    if (SIGNING_CEREMONY_NOT_COMPLETED.sameAs(error.getErrorCode())) {
                        throw new CantQueryStatusException(status, error.getErrorMessage());
                    }
                    throw new UnexpectedResponseException(error, Status.fromStatusCode(response.getStatus()), OK);
                } else {
                    throw exceptionForGeneralError(response);
                }
            }
        });
    }

    public InputStream getSignedDocumentStream(final String url) {
        return call(new Producer<InputStream>() {
            @Override
            InputStream call() {
                return parseResponse(httpClient.target(url).request().get(), InputStream.class);
            }
        });
    }

    public void cancel(final Cancellable cancellable) {
        call(new Runnable() {
            @Override
            public void run() {
                if (cancellable.getCancellationUrl() != null) {
                    String url = cancellable.getCancellationUrl().getUrl();
                    Response response = postEmptyEntity(url);
                    Status status = Status.fromStatusCode(response.getStatus());
                    if (status == OK) {
                        return;
                    } else if (status == CONFLICT) {
                        XMLError error = extractError(response);
                        throw new JobCannotBeCancelledException(status, error.getErrorCode(), error.getErrorMessage());
                    }
                    throw exceptionForGeneralError(response);
                } else {
                    throw new NotCancellableException();
                }
            }
        });
    }

    public XMLPortalSignatureJobStatusChangeResponse getStatusChange() {
        return call(new Producer<XMLPortalSignatureJobStatusChangeResponse>() {
            @Override
            XMLPortalSignatureJobStatusChangeResponse call() {
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
                    throw exceptionForGeneralError(response);
                }
            }
        });
    }

    public void confirm(final Confirmable confirmable) {
        call(new Runnable() {
            @Override
            public void run() {
                if (confirmable.getConfirmationReference() != null) {
                    String url = confirmable.getConfirmationReference().getConfirmationUrl();
                    LOG.info("Sends confirmation for '{}' to URL {}", confirmable, url);
                    Response response = postEmptyEntity(url);
                    Status status = Status.fromStatusCode(response.getStatus());
                    if (status != OK) {
                        throw exceptionForGeneralError(response);
                    }
                } else {
                    LOG.info("Does not need to send confirmation for '{}'", confirmable);
                }
            }
        });
    }

    private <T> T call(final Producer<T> producer) {
        try {
            return producer.call();
        } catch (ProcessingException e) {
            throw clientExceptionMapper.map(e);
        }
    }

    private void call(final Runnable function) {
        call(new Producer<Void>() {
            @Override
            Void call() {
                function.run();
                return null;
            }
        });
    }

    private abstract class Producer<T> {
        abstract T call();
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
            throw exceptionForGeneralError(response);
        }
    }

    private SignatureException exceptionForGeneralError(Response response) {
        XMLError error = extractError(response);
        if (BROKER_NOT_AUTHORIZED.sameAs(error.getErrorCode())) {
            return new BrokerNotAuthorizedException(error);
        }
        return new UnexpectedResponseException(error, Status.fromStatusCode(response.getStatus()), OK);
    }

    private static XMLError extractError(Response response) {
        XMLError error = null;
        String responseContentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        if (MediaType.valueOf(responseContentType).equals(APPLICATION_XML_TYPE)) {
            try {
                error = response.readEntity(XMLError.class);
            } catch (Exception e) {
                throw new UnexpectedResponseException(null, e, Status.fromStatusCode(response.getStatus()), OK);
            }
        } else {
            throw new UnexpectedResponseException(HttpHeaders.CONTENT_TYPE + " " + responseContentType + ": " + response.readEntity(String.class),
                    Status.fromStatusCode(response.getStatus()), OK);
        }
        if (error == null) {
            throw new UnexpectedResponseException(null, Status.fromStatusCode(response.getStatus()), OK);
        }
        return error;
    }
}
