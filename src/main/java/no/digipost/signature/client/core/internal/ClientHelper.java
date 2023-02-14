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
import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.BrokerNotAuthorizedException;
import no.digipost.signature.client.core.exceptions.CantQueryStatusException;
import no.digipost.signature.client.core.exceptions.DocumentsNotDeletableException;
import no.digipost.signature.client.core.exceptions.InvalidStatusQueryTokenException;
import no.digipost.signature.client.core.exceptions.JobCannotBeCancelledException;
import no.digipost.signature.client.core.exceptions.NotCancellableException;
import no.digipost.signature.client.core.exceptions.SignatureException;
import no.digipost.signature.client.core.exceptions.TooEagerPollingException;
import no.digipost.signature.client.core.exceptions.UnexpectedResponseException;
import no.digipost.signature.client.core.internal.http.ResponseStatus;
import no.digipost.signature.client.core.internal.http.SignatureHttpClient;
import no.digipost.signature.client.core.internal.http.StatusCode;
import no.digipost.signature.client.core.internal.http.StatusCodeFamily;
import no.digipost.signature.client.core.internal.xml.Marshalling;
import no.digipost.signature.client.direct.WithSignerUrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.MultipartPartBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static no.digipost.signature.client.core.internal.ActualSender.getActualSender;
import static no.digipost.signature.client.core.internal.ErrorCodes.BROKER_NOT_AUTHORIZED;
import static no.digipost.signature.client.core.internal.ErrorCodes.SIGNING_CEREMONY_NOT_COMPLETED;
import static no.digipost.signature.client.core.internal.Target.DIRECT;
import static no.digipost.signature.client.core.internal.Target.PORTAL;
import static no.digipost.signature.client.core.internal.http.StatusCodeFamily.SUCCESSFUL;
import static org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;
import static org.apache.hc.core5.http.ContentType.MULTIPART_MIXED;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;

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

        return multipartSignatureJobRequest(signatureJobRequest, documentBundle, actualSender, DIRECT, XMLDirectSignatureJobResponse.class);
    }

    public XMLPortalSignatureJobResponse sendPortalSignatureJobRequest(XMLPortalSignatureJobRequest signatureJobRequest, DocumentBundle documentBundle, Optional<Sender> sender) {
        final Sender actualSender = getActualSender(sender, globalSender);

        return multipartSignatureJobRequest(signatureJobRequest, documentBundle, actualSender, PORTAL, XMLPortalSignatureJobResponse.class);
    }

    private <RESPONSE, REQUEST> RESPONSE multipartSignatureJobRequest(REQUEST signatureJobRequest, DocumentBundle documentBundle, Sender actualSender, Target target, Class<RESPONSE> responseClass) {
        return call(() -> {
            try {
                var multipartEntityBuilder = MultipartEntityBuilder.create();
                multipartEntityBuilder.setContentType(MULTIPART_MIXED);

                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    Marshalling.marshal(signatureJobRequest, os);
                    multipartEntityBuilder.addPart(MultipartPartBuilder.create()
                            .setBody(new ByteArrayBody(os.toByteArray(), APPLICATION_XML, ""))
                            .addHeader(CONTENT_TYPE, APPLICATION_XML.getMimeType())
                            .build());
                }
                multipartEntityBuilder.addPart(MultipartPartBuilder.create()
                        .setBody(new InputStreamBody(documentBundle.getInputStream(), APPLICATION_OCTET_STREAM, ""))
                        .addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM.getMimeType()).build());

                try (HttpEntity multiPart = multipartEntityBuilder.build()) {
                    var request = ClassicRequestBuilder
                            .post(new URIBuilder(httpClient.signatureServiceRoot()).appendPath(target.path(actualSender)).build())
                            .addHeader(ACCEPT, APPLICATION_XML.getMimeType())
                            .build();

                    request.setEntity(multiPart);

                    return httpClient.httpClient().execute(request, response -> parseResponse(response, responseClass));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public XMLDirectSignerResponse requestNewRedirectUrl(WithSignerUrl url) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Marshalling.marshal(new XMLDirectSignerUpdateRequest().withRedirectUrl(new XMLEmptyElement()), os);
            var request = new HttpPost(url.getSignerUrl());
            request.addHeader(ACCEPT, APPLICATION_XML.getMimeType());

            return httpClient.httpClient().execute(request, response -> parseResponse(response, XMLDirectSignerResponse.class));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(final URI statusUrl) {
        return call(() -> {
            var request = new HttpGet(statusUrl);
            request.addHeader(ACCEPT, APPLICATION_XML.getMimeType());

            try {
                return httpClient.httpClient().execute(request, response -> {
                    ResponseStatus.resolve(response.getCode()).expect(StatusCodeFamily.SUCCESSFUL).orThrow(status -> {
                        if (status.value() == HttpStatus.SC_FORBIDDEN) {
                            XMLError error = extractError(response);
                            if (ErrorCodes.INVALID_STATUS_QUERY_TOKEN.sameAs(error.getErrorCode())) {
                                return new InvalidStatusQueryTokenException(statusUrl, error.getErrorMessage());
                            }
                        } else if (status.value() == HttpStatus.SC_NOT_FOUND) {
                            XMLError error = extractError(response);
                            if (SIGNING_CEREMONY_NOT_COMPLETED.sameAs(error.getErrorCode())) {
                                return new CantQueryStatusException(status, error.getErrorMessage());
                            }
                        }
                        return exceptionForGeneralError(response);
                    });
                    return parseResponse(response, XMLDirectSignatureJobStatusResponse.class);
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public ResponseInputStream getDataStream(URI uri, ContentType ... acceptedResponses) {
        return call(() -> {
            var acceptHeader = new HeaderGroup();
            Stream.of(acceptedResponses).map(ContentType::getMimeType).map(acceptedMimeType -> new BasicHeader(ACCEPT, acceptedMimeType)).forEach(acceptHeader::addHeader);
            var request = ClassicRequestBuilder.get(uri).addHeader(acceptHeader.getCondensedHeader(ACCEPT)).build();

            ClassicHttpResponse response = null;
            try {
                response = httpClient.httpClient().execute(null, request);
                var statusCode = StatusCode.from(response.getCode());
                if (!statusCode.is(SUCCESSFUL)) {
                    throw exceptionForGeneralError(response);
                }
                return new ResponseInputStream(
                        response.getEntity().getContent(),
                        Math.toIntExact(response.getEntity().getContentLength()));
            } catch (Exception e) {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException closingException) {
                        e.addSuppressed(closingException);
                    }
                }
                throw e instanceof RuntimeException
                    ? (RuntimeException) e
                    : new RuntimeException("uri: " + uri + ": " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
            }
        });
    }

    public void cancel(final Cancellable cancellable) {
        call(() -> {
            if (cancellable.getCancellationUrl() != null) {
                try(var response = postEmptyEntity(cancellable.getCancellationUrl().getUrl())) {
                    ResponseStatus.resolve(response.getCode())
                            .throwIf(HttpStatus.SC_CONFLICT, status -> new JobCannotBeCancelledException(status, extractError(response)))
                            .expect(StatusCodeFamily.SUCCESSFUL)
                            .orThrow(status -> exceptionForGeneralError(response));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
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

            try {
                var uri = new URIBuilder(httpClient.signatureServiceRoot())
                        .appendPath(target.path(actualSender))
                        .addParameter(POLLING_QUEUE_QUERY_PARAMETER, actualSender.getPollingQueue().value)
                        .build();
                var get = ClassicRequestBuilder
                        .get(uri)
                        .addHeader(ACCEPT, APPLICATION_XML.getMimeType())
                        .build();
                return httpClient.httpClient().execute(get, response -> {
                    var status = ResponseStatus.resolve(response.getCode())
                            .throwIf(HttpStatus.SC_TOO_MANY_REQUESTS, s -> new TooEagerPollingException())
                            .expect(StatusCodeFamily.SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response));
                    return new JobStatusResponse<>(status.value() == HttpStatus.SC_NO_CONTENT ? null : Marshalling.unmarshal(response.getEntity().getContent(), responseClass), getNextPermittedPollTime(response));
                });
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not create uri, because " + e.getMessage(), e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static Instant getNextPermittedPollTime(ClassicHttpResponse response) throws ProtocolException {
        return ZonedDateTime.parse(response.getHeader(NEXT_PERMITTED_POLL_TIME_HEADER).getValue(), ISO_DATE_TIME).toInstant();
    }

    public void confirm(final Confirmable confirmable) {
        call(() -> {
            if (confirmable.getConfirmationReference() != null) {
                URI url = confirmable.getConfirmationReference().getConfirmationUrl();
                LOG.info("Sends confirmation for '{}' to URL {}", confirmable, url);
                try (var response = postEmptyEntity(url)) {
                    ResponseStatus.resolve(response.getCode()).expect(StatusCodeFamily.SUCCESSFUL).orThrow(status -> exceptionForGeneralError(response));
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
                var url = deleteDocumentsUrl.getUrl();
                try (var response = delete(url)) {
                    ResponseStatus.resolve(response.getCode()).expect(StatusCodeFamily.SUCCESSFUL).orThrow(status -> exceptionForGeneralError(response));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                throw new DocumentsNotDeletableException();
            }
        });
    }

    private ClassicHttpResponse postEmptyEntity(URI uri) {
        try {
            var request = ClassicRequestBuilder
                    .post(uri)
                    .addHeader(ACCEPT, APPLICATION_XML.getMimeType())
                    .build();

            return httpClient.httpClient().execute(request, response -> response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ClassicHttpResponse delete(URI uri) {
        try {
            var request = ClassicRequestBuilder
                    .delete(uri)
                    .addHeader(ACCEPT, APPLICATION_XML.getMimeType())
                    .build();

            return httpClient.httpClient().execute(request, response -> response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> T parseResponse(ClassicHttpResponse response, Class<T> responseType) {
        ResponseStatus.resolve(response.getCode()).expect(StatusCodeFamily.SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response));
        try(var body = response.getEntity().getContent()) {
            return Marshalling.unmarshal(body, responseType);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not parse response.", e);
        }
    }

    private static SignatureException exceptionForGeneralError(ClassicHttpResponse response) {
        XMLError error = extractError(response);
        if (BROKER_NOT_AUTHORIZED.sameAs(error.getErrorCode())) {
            return new BrokerNotAuthorizedException(error);
        }
        return new UnexpectedResponseException(error, ResponseStatus.resolve(response.getCode()).get(), StatusCode.from(HttpStatus.SC_OK));
    }

    private static XMLError extractError(ClassicHttpResponse response) {
        try {
            XMLError error;
            var contentType = Optional.ofNullable(response.getHeader(HttpHeaders.CONTENT_TYPE)).map(NameValuePair::getValue).map(ContentType::create);
            if (contentType.map(type -> type.equals(APPLICATION_XML)).orElse(false)) {
                try(var body = response.getEntity().getContent()) {
                    error = Marshalling.unmarshal(body, XMLError.class);
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not extract error from body.", e);
                }
            } else {
                String errorAsString;
                try(var body = response.getEntity().getContent()) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int length; (length = body.read(buffer)) != -1; ) {
                        result.write(buffer, 0, length);
                    }
                    errorAsString = result.toString(StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not read body as string.", e);
                }
                throw new UnexpectedResponseException(
                        HttpHeaders.CONTENT_TYPE + " " + contentType.map(ContentType::getMimeType).orElse("unknown") + ": " +
                        Optional.ofNullable(errorAsString).filter(StringUtils::isNoneBlank).orElse("<no content in response>"),
                        ResponseStatus.resolve(response.getCode()).get(), StatusCode.from(HttpStatus.SC_OK));
            }
            return error;
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
    }

}
