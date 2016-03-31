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
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.*;
import no.digipost.signature.client.core.internal.http.SignatureHttpClient;
import no.motif.single.Optional;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.*;
import static no.digipost.signature.client.core.exceptions.SenderNotSpecifiedException.SENDER_NOT_SPECIFIED;
import static no.digipost.signature.client.core.internal.ErrorCodes.BROKER_NOT_AUTHORIZED;
import static no.digipost.signature.client.core.internal.ErrorCodes.SIGNING_CEREMONY_NOT_COMPLETED;
import static no.digipost.signature.client.core.internal.Target.DIRECT;
import static no.digipost.signature.client.core.internal.Target.PORTAL;
import static no.motif.Singular.optional;
import static no.motif.Strings.nonblank;

public class ClientHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);

    public static final String NEXT_PERMITTED_POLL_TIME_HEADER = "X-Next-permitted-poll-time";

    private final SignatureHttpClient httpClient;
    private final Optional<Sender> globalSender;
    private final ClientExceptionMapper clientExceptionMapper;

    public ClientHelper(SignatureHttpClient httpClient, Optional<Sender> globalSender) {
        this.httpClient = httpClient;
        this.globalSender = globalSender;
        this.clientExceptionMapper = new ClientExceptionMapper();
    }

    public XMLDirectSignatureJobResponse sendSignatureJobRequest(XMLDirectSignatureJobRequest signatureJobRequest, DocumentBundle documentBundle, Optional<Sender> sender) {
        final Sender actualSender = sender.or(globalSender).orElseThrow(SENDER_NOT_SPECIFIED);

        final BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        final BodyPart documentBundleBodyPart = new BodyPart(documentBundle.getInputStream(), APPLICATION_OCTET_STREAM_TYPE);

        return call(new Callable<XMLDirectSignatureJobResponse>() {
            @Override
            public XMLDirectSignatureJobResponse call() {
                return new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                        .postAsMultiPart(DIRECT.path(actualSender), XMLDirectSignatureJobResponse.class);
            }
        });
    }

    public XMLPortalSignatureJobResponse sendPortalSignatureJobRequest(XMLPortalSignatureJobRequest signatureJobRequest, DocumentBundle documentBundle, Optional<Sender> sender) {
        final Sender actualSender = sender.or(globalSender).orElseThrow(SENDER_NOT_SPECIFIED);

        final BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        final BodyPart documentBundleBodyPart = new BodyPart(documentBundle.getInputStream(), APPLICATION_OCTET_STREAM_TYPE);

        return call(new Callable<XMLPortalSignatureJobResponse>() {
            @Override
            public XMLPortalSignatureJobResponse call() {
                return new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                        .postAsMultiPart(PORTAL.path(actualSender), XMLPortalSignatureJobResponse.class);
            }
        });
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(final String statusUrl) {
        return call(new Callable<XMLDirectSignatureJobStatusResponse>() {
            @Override
            public XMLDirectSignatureJobStatusResponse call() {
                Response response = httpClient.target(statusUrl)
                        .request()
                        .accept(APPLICATION_XML_TYPE)
                        .get();
                try {
                    Status status = Status.fromStatusCode(response.getStatus());
                    if (status == OK) {
                        return response.readEntity(XMLDirectSignatureJobStatusResponse.class);
                    } else if (status == FORBIDDEN) {
                        XMLError error = extractError(response);
                        if (ErrorCodes.INVALID_STATUS_QUERY_TOKEN.sameAs(error.getErrorCode())) {
                            throw new InvalidStatusQueryTokenException(statusUrl, error.getErrorMessage());
                        }
                    } else if (status == NOT_FOUND) {
                        XMLError error = extractError(response);
                        if (SIGNING_CEREMONY_NOT_COMPLETED.sameAs(error.getErrorCode())) {
                            throw new CantQueryStatusException(status, error.getErrorMessage());
                        }
                    }
                    throw exceptionForGeneralError(response);
                } finally {
                    response.close();
                }
            }
        });
    }

    public InputStream getSignedDocumentStream(final String uri) {
        return call(new Callable<InputStream>() {
            @Override
            public InputStream call() {
                return parseResponse(httpClient.target(uri).request().accept(APPLICATION_XML_TYPE, APPLICATION_OCTET_STREAM_TYPE).get(), InputStream.class);
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
                    try {
                        Status status = Status.fromStatusCode(response.getStatus());
                        if (status == OK) {
                            return;
                        } else if (status == CONFLICT) {
                            XMLError error = extractError(response);
                            throw new JobCannotBeCancelledException(status, error.getErrorCode(), error.getErrorMessage());
                        }
                        throw exceptionForGeneralError(response);
                    } finally {
                        response.close();
                    }
                } else {
                    throw new NotCancellableException();
                }
            }
        });
    }

    public XMLPortalSignatureJobStatusChangeResponse getStatusChange() {
        return getStatusChange(globalSender.orElseThrow(SENDER_NOT_SPECIFIED));
    }

    public XMLPortalSignatureJobStatusChangeResponse getStatusChange(final Sender sender) {
        return call(new Callable<XMLPortalSignatureJobStatusChangeResponse>() {
            @Override
            public XMLPortalSignatureJobStatusChangeResponse call() {
                Response response = httpClient.signatureServiceRoot().path(PORTAL.path(sender))
                        .request()
                        .accept(APPLICATION_XML_TYPE)
                        .get();
                try {
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
                } finally {
                    response.close();
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
                    try {
                        Status status = Status.fromStatusCode(response.getStatus());
                        if (status != OK) {
                            throw exceptionForGeneralError(response);
                        }
                    } finally {
                        response.close();
                    }
                } else {
                    LOG.info("Does not need to send confirmation for '{}'", confirmable);
                }
            }
        });
    }

    private <T> T call(Callable<T> producer) {
        return clientExceptionMapper.doWithMappedClientException(producer);
    }

    private void call(Runnable action) {
        clientExceptionMapper.doWithMappedClientException(action);
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

                Response response = httpClient.signatureServiceRoot().path(path)
                        .request()
                        .header(CONTENT_TYPE, multiPart.getMediaType())
                        .accept(APPLICATION_XML_TYPE)
                        .post(Entity.entity(multiPart, multiPart.getMediaType()));
                try {
                    return parseResponse(response, responseType);
                } finally {
                    response.close();
                }
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }
    }

    private Response postEmptyEntity(String uri) {
        return httpClient.target(uri)
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
        Optional<String> responseContentType = optional(response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        if (responseContentType.isSome() && MediaType.valueOf(responseContentType.get()).equals(APPLICATION_XML_TYPE)) {
            try {
                response.bufferEntity();
                error = response.readEntity(XMLError.class);
            } catch (Exception e) {
                throw new UnexpectedResponseException(
                        HttpHeaders.CONTENT_TYPE + " " + responseContentType.orElse("unknown") + ": " +
                        optional(nonblank, response.readEntity(String.class)).orElse("<no content in response>"),
                        e, Status.fromStatusCode(response.getStatus()), OK);
            }
        } else {
            throw new UnexpectedResponseException(
                    HttpHeaders.CONTENT_TYPE + " " + responseContentType.orElse("unknown") + ": " +
                    optional(nonblank, response.readEntity(String.class)).orElse("<no content in response>"),
                    Status.fromStatusCode(response.getStatus()), OK);
        }
        if (error == null) {
            throw new UnexpectedResponseException(null, Status.fromStatusCode(response.getStatus()), OK);
        }
        return error;
    }
}
