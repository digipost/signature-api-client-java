package no.digipost.signature.client.core.internal;

import no.digipost.signature.api.xml.XMLDirectSignatureJobRequest;
import no.digipost.signature.api.xml.XMLDirectSignatureJobResponse;
import no.digipost.signature.api.xml.XMLDirectSignatureJobStatusResponse;
import no.digipost.signature.api.xml.XMLDirectSignerResponse;
import no.digipost.signature.api.xml.XMLDirectSignerUpdateRequest;
import no.digipost.signature.api.xml.XMLEmptyElement;
import no.digipost.signature.api.xml.XMLError;
import no.digipost.signature.api.xml.XMLPortalSignatureJobRequest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.api.xml.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.core.DeleteDocumentsUrl;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.BrokerNotAuthorizedException;
import no.digipost.signature.client.core.exceptions.CantQueryStatusException;
import no.digipost.signature.client.core.exceptions.DocumentsNotDeletableException;
import no.digipost.signature.client.core.exceptions.InvalidStatusQueryTokenException;
import no.digipost.signature.client.core.exceptions.JobCannotBeCancelledException;
import no.digipost.signature.client.core.exceptions.NotCancellableException;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.exceptions.SignatureException;
import no.digipost.signature.client.core.exceptions.TooEagerPollingException;
import no.digipost.signature.client.core.exceptions.UnexpectedResponseException;
import no.digipost.signature.client.core.internal.http.ResponseStatus;
import no.digipost.signature.client.core.internal.http.SignatureHttpClient;
import no.digipost.signature.client.direct.WithSignerUrl;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS;
import static no.digipost.signature.client.core.internal.ActualSender.getActualSender;
import static no.digipost.signature.client.core.internal.ErrorCodes.BROKER_NOT_AUTHORIZED;
import static no.digipost.signature.client.core.internal.ErrorCodes.SIGNING_CEREMONY_NOT_COMPLETED;
import static no.digipost.signature.client.core.internal.Target.DIRECT;
import static no.digipost.signature.client.core.internal.Target.PORTAL;

public class ClientHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);

    private static final String NEXT_PERMITTED_POLL_TIME_HEADER = "X-Next-permitted-poll-time";
    private static final String POLLING_QUEUE_QUERY_PARAMETER = "polling_queue";

    private final SignatureHttpClient httpClient;
    private final Optional<Sender> globalSender;
    private final ClientExceptionMapper clientExceptionMapper;

    public ClientHelper(SignatureHttpClient httpClient, Optional<Sender> globalSender) {
        this.httpClient = httpClient;
        this.globalSender = globalSender;
        this.clientExceptionMapper = new ClientExceptionMapper();
    }

    public XMLDirectSignatureJobResponse sendSignatureJobRequest(XMLDirectSignatureJobRequest signatureJobRequest, DocumentBundle documentBundle, Optional<Sender> sender) {
        final Sender actualSender = getActualSender(sender, globalSender);

        final BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        final BodyPart documentBundleBodyPart = new BodyPart(documentBundle.getInputStream(), APPLICATION_OCTET_STREAM_TYPE);

        return call(() -> new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                .postAsMultiPart(DIRECT.path(actualSender), XMLDirectSignatureJobResponse.class));
    }

    public XMLDirectSignerResponse requestNewRedirectUrl(WithSignerUrl url) {
        try (Response response = postEntity(url.getSignerUrl(), new XMLDirectSignerUpdateRequest().withRedirectUrl(new XMLEmptyElement()))) {
            return parseResponse(response, XMLDirectSignerResponse.class);
        }
    }

    public XMLPortalSignatureJobResponse sendPortalSignatureJobRequest(XMLPortalSignatureJobRequest signatureJobRequest, DocumentBundle documentBundle, Optional<Sender> sender) {
        final Sender actualSender = getActualSender(sender, globalSender);

        final BodyPart signatureJobBodyPart = new BodyPart(signatureJobRequest, APPLICATION_XML_TYPE);
        final BodyPart documentBundleBodyPart = new BodyPart(documentBundle.getInputStream(), APPLICATION_OCTET_STREAM_TYPE);

        return call(() -> new UsingBodyParts(signatureJobBodyPart, documentBundleBodyPart)
                .postAsMultiPart(PORTAL.path(actualSender), XMLPortalSignatureJobResponse.class));
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(final URI statusUrl) {
        return call(() -> {
            Invocation.Builder request = httpClient.target(statusUrl).request().accept(APPLICATION_XML_TYPE);

            try (Response response = request.get()) {
                StatusType status = ResponseStatus.resolve(response.getStatus());
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
            }
        });
    }

    public InputStream getSignedDocumentStream(final URI uri) {
        return call(() -> parseResponse(httpClient.target(uri).request().accept(APPLICATION_XML_TYPE, APPLICATION_OCTET_STREAM_TYPE).get(), InputStream.class));
    }

    public void cancel(final Cancellable cancellable) {
        call(() -> {
            if (cancellable.getCancellationUrl() != null) {
                URI url = cancellable.getCancellationUrl().getUrl();
                try (Response response = postEmptyEntity(url)) {
                    StatusType status = ResponseStatus.resolve(response.getStatus());
                    if (status == OK) {
                        return;
                    } else if (status == CONFLICT) {
                        XMLError error = extractError(response);
                        throw new JobCannotBeCancelledException(status, error.getErrorCode(), error.getErrorMessage());
                    }
                    throw exceptionForGeneralError(response);
                }
            } else {
                throw new NotCancellableException();
            }
        });
    }

    public JobStatusResponse<XMLPortalSignatureJobStatusChangeResponse> getPortalStatusChange(Optional<Sender> sender) {
        return getStatusChange(sender, PORTAL, XMLPortalSignatureJobStatusChangeResponse.class);
    }

    public JobStatusResponse<XMLDirectSignatureJobStatusResponse> getDirectStatusChange(Optional<Sender> sender) {
        return getStatusChange(sender, DIRECT, XMLDirectSignatureJobStatusResponse.class);
    }

    private <RESPONSE_CLASS> JobStatusResponse<RESPONSE_CLASS> getStatusChange(final Optional<Sender> sender, final Target target, final Class<RESPONSE_CLASS> responseClass) {
        return call(() -> {
            Sender actualSender = getActualSender(sender, globalSender);
            Invocation.Builder request = httpClient.signatureServiceRoot().path(target.path(actualSender))
                    .queryParam(POLLING_QUEUE_QUERY_PARAMETER, actualSender.getPollingQueue().value)
                    .request()
                    .accept(APPLICATION_XML_TYPE);
            try (Response response = request.get()) {
                StatusType status = ResponseStatus.resolve(response.getStatus());
                if (status == NO_CONTENT) {
                    return new JobStatusResponse<>(null, getNextPermittedPollTime(response));
                } else if (status == OK) {
                    return new JobStatusResponse<>(response.readEntity(responseClass), getNextPermittedPollTime(response));
                } else if (status == TOO_MANY_REQUESTS) {
                    throw new TooEagerPollingException();
                } else {
                    throw exceptionForGeneralError(response);
                }
            }
        });
    }

    private static Instant getNextPermittedPollTime(Response response) {
        return ZonedDateTime.parse(response.getHeaderString(NEXT_PERMITTED_POLL_TIME_HEADER), ISO_DATE_TIME).toInstant();
    }

    public void confirm(final Confirmable confirmable) {
        call(() -> {
            if (confirmable.getConfirmationReference() != null) {
                URI url = confirmable.getConfirmationReference().getConfirmationUrl();
                LOG.info("Sends confirmation for '{}' to URL {}", confirmable, url);
                try (Response response = postEmptyEntity(url)) {
                    StatusType status = ResponseStatus.resolve(response.getStatus());
                    if (status != OK) {
                        throw exceptionForGeneralError(response);
                    }
                }
            } else {
                LOG.info("Does not need to send confirmation for '{}'", confirmable);
            }
        });
    }

    private <T> T call(Supplier<T> supplier) {
        return clientExceptionMapper.doWithMappedClientException(supplier);
    }

    private void call(Runnable action) {
        clientExceptionMapper.doWithMappedClientException(action);
    }

    public void deleteDocuments(DeleteDocumentsUrl deleteDocumentsUrl) {
        call(() -> {
            if (deleteDocumentsUrl != null) {
                URI url = deleteDocumentsUrl.getUrl();
                try (Response response = delete(url)) {
                    StatusType status = ResponseStatus.resolve(response.getStatus());
                    if (status == OK) {
                        return;
                    }
                    throw exceptionForGeneralError(response);
                }
            } else {
                throw new DocumentsNotDeletableException();
            }
        });

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

                Invocation.Builder request = httpClient.signatureServiceRoot().path(path)
                        .request()
                        .header(CONTENT_TYPE, multiPart.getMediaType())
                        .accept(APPLICATION_XML_TYPE);
                try (Response response = request.post(Entity.entity(multiPart, multiPart.getMediaType()))) {
                    return parseResponse(response, responseType);
                }
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }
    }

    private Response postEmptyEntity(URI uri) {
        return postEntity(uri, null);
    }

    private Response postEntity(URI uri, Object entity) {
        Invocation.Builder requestBuilder = httpClient.target(uri)
                .request()
                .accept(APPLICATION_XML_TYPE);
        return (entity == null ? requestBuilder.header(CONTENT_LENGTH, 0) : requestBuilder)
                .post(Entity.entity(entity, APPLICATION_XML_TYPE));
    }


    private Response delete(URI uri) {
        return httpClient.target(uri)
                .request()
                .accept(APPLICATION_XML_TYPE)
                .delete();
    }

    private <T> T parseResponse(Response response, Class<T> responseType) {
        StatusType status = ResponseStatus.resolve(response.getStatus());
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
        return new UnexpectedResponseException(error, ResponseStatus.resolve(response.getStatus()), OK);
    }

    private static XMLError extractError(Response response) {
        XMLError error;
        Optional<String> responseContentType = Optional.ofNullable(response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        if (responseContentType.isPresent() && MediaType.valueOf(responseContentType.get()).equals(APPLICATION_XML_TYPE)) {
            try {
                response.bufferEntity();
                error = response.readEntity(XMLError.class);
            } catch (Exception e) {
                throw new UnexpectedResponseException(
                        HttpHeaders.CONTENT_TYPE + " " + responseContentType.orElse("unknown") + ": " +
                        Optional.ofNullable(response.readEntity(String.class)).filter(StringUtils::isNoneBlank).orElse("<no content in response>"),
                        e, ResponseStatus.resolve(response.getStatus()), OK);
            }
        } else {
            throw new UnexpectedResponseException(
                    HttpHeaders.CONTENT_TYPE + " " + responseContentType.orElse("unknown") + ": " +
                    Optional.ofNullable(response.readEntity(String.class)).filter(StringUtils::isNoneBlank).orElse("<no content in response>"),
                    ResponseStatus.resolve(response.getStatus()), OK);
        }
        if (error == null) {
            throw new UnexpectedResponseException(null, ResponseStatus.resolve(response.getStatus()), OK);
        }
        return error;
    }

}
