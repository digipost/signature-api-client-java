package no.digipost.signature.client.core.internal;

import no.digipost.signature.api.xml.XMLDirectSignatureJobStatusResponse;
import no.digipost.signature.api.xml.XMLDirectSignerResponse;
import no.digipost.signature.api.xml.XMLDirectSignerUpdateRequest;
import no.digipost.signature.api.xml.XMLEmptyElement;
import no.digipost.signature.api.xml.XMLError;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.core.DeleteDocumentsUrl;
import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.BrokerNotAuthorizedException;
import no.digipost.signature.client.core.exceptions.CantQueryStatusException;
import no.digipost.signature.client.core.exceptions.DocumentsNotDeletableException;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.core.exceptions.InvalidStatusQueryTokenException;
import no.digipost.signature.client.core.exceptions.JobCannotBeCancelledException;
import no.digipost.signature.client.core.exceptions.NotCancellableException;
import no.digipost.signature.client.core.exceptions.SignatureException;
import no.digipost.signature.client.core.exceptions.TooEagerPollingException;
import no.digipost.signature.client.core.exceptions.UnexpectedResponseException;
import no.digipost.signature.client.core.internal.http.ResponseStatus;
import no.digipost.signature.client.core.internal.http.SignatureServiceRoot;
import no.digipost.signature.client.core.internal.http.StatusCode;
import no.digipost.signature.client.core.internal.xml.Marshalling;
import no.digipost.signature.client.direct.WithSignerUrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.MultipartPartBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static no.digipost.signature.client.core.internal.ErrorCodes.BROKER_NOT_AUTHORIZED;
import static no.digipost.signature.client.core.internal.ErrorCodes.INVALID_STATUS_QUERY_TOKEN;
import static no.digipost.signature.client.core.internal.ErrorCodes.SIGNING_CEREMONY_NOT_COMPLETED;
import static no.digipost.signature.client.core.internal.http.StatusCode.CONFLICT;
import static no.digipost.signature.client.core.internal.http.StatusCode.NO_CONTENT;
import static no.digipost.signature.client.core.internal.http.StatusCode.TOO_MANY_REQUESTS;
import static no.digipost.signature.client.core.internal.http.StatusCode.Family.SUCCESSFUL;
import static org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;
import static org.apache.hc.core5.http.ContentType.MULTIPART_MIXED;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;

public class ClientHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);

    private static final String NEXT_PERMITTED_POLL_TIME_HEADER = "X-Next-permitted-poll-time";
    private static final String POLLING_QUEUE_QUERY_PARAMETER = "polling_queue";

    private final SignatureServiceRoot serviceRoot;
    private final HttpClient httpClient;
    private final ClientExceptionMapper clientExceptionMapper;


    public ClientHelper(SignatureServiceRoot serviceRoot, HttpClient httpClient) {
        this.serviceRoot = serviceRoot;
        this.httpClient = httpClient;
        this.clientExceptionMapper = new ClientExceptionMapper();
    }

    public <RESPONSE, REQUEST> RESPONSE sendSignatureJobRequest(ApiFlow<REQUEST, RESPONSE, ?> target, REQUEST signatureJobRequest, DocumentBundle documentBundle, Sender sender) {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setContentType(MULTIPART_MIXED);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Marshalling.marshal(signatureJobRequest, os);
            multipartEntityBuilder.addPart(MultipartPartBuilder.create()
                    .setBody(new ByteArrayBody(os.toByteArray(), APPLICATION_XML, ""))
                    .addHeader(CONTENT_TYPE, APPLICATION_XML.getMimeType())
                    .build());

            multipartEntityBuilder.addPart(MultipartPartBuilder.create()
                    .setBody(new InputStreamBody(documentBundle.getInputStream(), APPLICATION_OCTET_STREAM, ""))
                    .addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM.getMimeType()).build());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try (HttpEntity multiPart = multipartEntityBuilder.build()) {
            ClassicHttpRequest request = ClassicRequestBuilder
                    .post(serviceRoot.constructUrl(uri -> uri.appendPath(target.path(sender))))
                    .addHeader(ACCEPT, APPLICATION_XML.getMimeType())
                    .build();

            request.setEntity(multiPart);
            return call(() -> {
                try {
                    return httpClient.execute(request, response -> parseResponse(response, target.apiResponseType));
                } catch (IOException e) {
                    throw new HttpIOException(request, e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public XMLDirectSignerResponse requestNewRedirectUrl(WithSignerUrl url) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Marshalling.marshal(new XMLDirectSignerUpdateRequest().withRedirectUrl(new XMLEmptyElement()), os);
            ClassicHttpRequest request = new HttpPost(url.getSignerUrl());
            request.addHeader(ACCEPT, APPLICATION_XML.getMimeType());

            return httpClient.execute(request, response -> parseResponse(response, XMLDirectSignerResponse.class));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(URI statusUrl) {
        ClassicHttpRequest request = new HttpGet(statusUrl);
        request.addHeader(ACCEPT, APPLICATION_XML.getMimeType());

        return call(() -> {
            try {
                return httpClient.execute(request, response -> {
                    ResponseStatus.fromHttpResponse(response).expect(SUCCESSFUL).orThrow(status -> {
                        if (status.value() == HttpStatus.SC_FORBIDDEN) {
                            XMLError error = extractError(response);
                            if (INVALID_STATUS_QUERY_TOKEN.sameAs(error.getErrorCode())) {
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
                throw new HttpIOException(request, e);
            }
        });
    }

    public ResponseInputStream getDataStream(String path, ContentType ... acceptedResponses) {
        return getDataStream(serviceRoot.constructUrl(uri -> uri.appendPath(path)));
    }

    public ResponseInputStream getDataStream(URI absoluteUri, ContentType ... acceptedResponses) {
        if (!absoluteUri.isAbsolute()) {
            throw new IllegalArgumentException("'" + absoluteUri + "' is not an absolute URL");
        }
        HeaderGroup acceptHeader = new HeaderGroup();
        for (ContentType acceptedType : acceptedResponses) {
            acceptHeader.addHeader(new BasicHeader(ACCEPT, acceptedType.getMimeType()));
        }

        ClassicHttpRequest request = ClassicRequestBuilder.get(absoluteUri)
                .addHeader(acceptHeader.getCondensedHeader(ACCEPT))
                .build();

        return call(() -> {
            ClassicHttpResponse response = null;
            try {
                response = httpClient.executeOpen(null, request, null);
                StatusCode statusCode = StatusCode.from(response.getCode());
                if (!statusCode.is(SUCCESSFUL)) {
                    throw exceptionForGeneralError(response);
                }
                return new ResponseInputStream(response.getEntity().getContent(), response.getEntity().getContentLength());
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
                    : new RuntimeException(request + ": " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
            }
        });
    }

    public void cancel(Cancellable cancellable) {
            if (cancellable.getCancellationUrl() != null) {
                postEmptyEntity(cancellable.getCancellationUrl().getUrl(), httpResponse -> ResponseStatus.fromHttpResponse(httpResponse)
                        .throwIf(CONFLICT, status -> new JobCannotBeCancelledException(status, extractError(httpResponse))));
            } else {
                throw new NotCancellableException();
            }
    }

    public <RES> JobStatusResponse<RES> getStatusChange(ApiFlow<?, ?, RES> target, Sender sender) {

        URI jobStatusUrl = serviceRoot.constructUrl(uri -> uri
                .appendPath(target.path(sender))
                .addParameter(POLLING_QUEUE_QUERY_PARAMETER, sender.getPollingQueue().value));

        ClassicHttpRequest getStatusRequest = ClassicRequestBuilder.get(jobStatusUrl).addHeader(ACCEPT, APPLICATION_XML.getMimeType()).build();
        return call(() -> {
            try {
                return httpClient.execute(getStatusRequest, response -> {
                    StatusCode status = ResponseStatus.fromHttpResponse(response)
                            .throwIf(TOO_MANY_REQUESTS, s -> new TooEagerPollingException())
                            .expect(SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response));
                    RES statusResponseBody = status.equals(NO_CONTENT) ? null : Marshalling.unmarshal(response.getEntity().getContent(), target.statusResponseType);
                    return new JobStatusResponse<>(statusResponseBody, getNextPermittedPollTime(response));
                });
            } catch (IOException e) {
                throw new HttpIOException(getStatusRequest, e);
            }
        });
    }

    private static Instant getNextPermittedPollTime(ClassicHttpResponse response) throws ProtocolException {
        return ZonedDateTime.parse(response.getHeader(NEXT_PERMITTED_POLL_TIME_HEADER).getValue(), ISO_DATE_TIME).toInstant();
    }

    public void confirm(final Confirmable confirmable) {
        if (confirmable.getConfirmationReference() != null) {
            URI url = confirmable.getConfirmationReference().getConfirmationUrl();
            LOG.info("Sends confirmation for '{}' to URL {}", confirmable, url);
            postEmptyEntity(url);
        } else {
            LOG.info("Does not need to send confirmation for '{}'", confirmable);
        }
    }

    public void deleteDocuments(DeleteDocumentsUrl deleteDocumentsUrl) {
        if (deleteDocumentsUrl != null) {
            delete(deleteDocumentsUrl.getUrl());
        } else {
            throw new DocumentsNotDeletableException();
        }
    }

    private void postEmptyEntity(URI uri) {
        postEmptyEntity(uri, ResponseStatus::fromHttpResponse);
    }

    private void postEmptyEntity(URI uri, Function<ClassicHttpResponse, ResponseStatus> responseStatusHandling) {
        ClassicHttpRequest request = ClassicRequestBuilder
                .post(uri)
                .addHeader(ACCEPT, APPLICATION_XML.getMimeType())
                .build();
        call(() ->  {
            try {
                httpClient.execute(request, response -> responseStatusHandling.apply(response)
                        .expect(SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response)));
            } catch (IOException e) {
                throw new HttpIOException(request, e);
            }
        });
    }

    private void delete(URI uri) {
        ClassicHttpRequest request = ClassicRequestBuilder
                .delete(uri)
                .addHeader(ACCEPT, APPLICATION_XML.getMimeType())
                .build();
        call(() ->  {
            try {
                httpClient.execute(request, response -> ResponseStatus.fromHttpResponse(response)
                        .expect(SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response)));
            } catch (IOException e) {
                throw new HttpIOException(request, e);
            }
        });
    }

    private static <T> T parseResponse(ClassicHttpResponse response, Class<T> responseType) {
        ResponseStatus.fromHttpResponse(response).expect(SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response));
        try (InputStream body = response.getEntity().getContent()) {
            return Marshalling.unmarshal(body, responseType);
        } catch (IOException e) {
            throw new HttpIOException(response, e);
        }
    }

    private static SignatureException exceptionForGeneralError(ClassicHttpResponse response) {
        XMLError error = extractError(response);
        if (BROKER_NOT_AUTHORIZED.sameAs(error.getErrorCode())) {
            return new BrokerNotAuthorizedException(error);
        }
        return new UnexpectedResponseException(error, ResponseStatus.fromHttpResponse(response).get(), StatusCode.OK);
    }

    private static XMLError extractError(ClassicHttpResponse response) {
        try {
            XMLError error;
            Optional<ContentType> contentType = Optional.ofNullable(response.getHeader(CONTENT_TYPE)).map(NameValuePair::getValue).map(ContentType::parse);
            if (contentType.filter(APPLICATION_XML::isSameMimeType).isPresent()) {
                try(InputStream body = response.getEntity().getContent()) {
                    error = Marshalling.unmarshal(body, XMLError.class);
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not extract error from body.", e);
                }
            } else {
                String errorAsString;
                try(InputStream body = response.getEntity().getContent()) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int length; (length = body.read(buffer)) != -1; ) {
                        result.write(buffer, 0, length);
                    }
                    errorAsString = result.toString(StandardCharsets.UTF_8.name());
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not read body as string.", e);
                }
                throw new UnexpectedResponseException(
                        HttpHeaders.CONTENT_TYPE + " " + contentType.map(ContentType::getMimeType).orElse("unknown") + ": " +
                        Optional.ofNullable(errorAsString).filter(StringUtils::isNoneBlank).orElse("<no content in response>"),
                        ResponseStatus.fromHttpResponse(response).get(), StatusCode.OK);
            }
            return error;
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T call(Supplier<T> supplier) {
        return clientExceptionMapper.doWithMappedClientException(supplier);
    }

    private void call(Runnable action) {
        clientExceptionMapper.doWithMappedClientException(action);
    }

}
