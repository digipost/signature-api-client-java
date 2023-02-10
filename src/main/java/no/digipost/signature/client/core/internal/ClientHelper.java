package no.digipost.signature.client.core.internal;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.core.UriBuilder;
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
import no.digipost.signature.client.core.internal.xml.Marshalling;
import no.digipost.signature.client.direct.WithSignerUrl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.TOO_MANY_REQUESTS;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
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

        return multipartSignatureJobRequest(signatureJobRequest, documentBundle, actualSender, XMLDirectSignatureJobResponse.class);
    }

    public XMLPortalSignatureJobResponse sendPortalSignatureJobRequest(XMLPortalSignatureJobRequest signatureJobRequest, DocumentBundle documentBundle, Optional<Sender> sender) {
        final Sender actualSender = getActualSender(sender, globalSender);

        return multipartSignatureJobRequest(signatureJobRequest, documentBundle, actualSender, XMLPortalSignatureJobResponse.class);
    }

    // TODO: Add TARGET, not only use DIRECT
    private <RESPONSE, REQUEST> RESPONSE multipartSignatureJobRequest(REQUEST signatureJobRequest, DocumentBundle documentBundle, Sender actualSender, Class<RESPONSE> responseClass) {
        return call(() -> {
            try {
                String boundary = new BigInteger(256, new Random()).toString();
                var byteArrays = new ArrayList<byte[]>();
                byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
                        .getBytes(StandardCharsets.UTF_8);

                byteArrays.add(separator);

                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    Marshalling.marshal(signatureJobRequest, os);
                    byteArrays.add(("\"\r\nContent-Type: " + APPLICATION_XML_TYPE.getType() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    byteArrays.add(os.toByteArray());
                }

                byteArrays.add(separator);

                byteArrays.add(("\"\r\nContent-Type: " + APPLICATION_OCTET_STREAM_TYPE.getType() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(documentBundle.getInputStream().readAllBytes());

                byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

                var request = HttpRequest.newBuilder()
                        .uri(UriBuilder.fromUri(httpClient.signatureServiceRoot()).path(DIRECT.path(actualSender)).build())
                        .header("Content-Type", "multipart/mixed;boundary=" + boundary)
                        .header(ACCEPT, APPLICATION_XML_TYPE.getType())
                        .POST(HttpRequest.BodyPublishers.ofByteArrays(byteArrays))
                        .build();

                var response = httpClient.httpClient().send(request, BodyHandlers.ofInputStream());
                return parseResponse(response, responseClass);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public XMLDirectSignerResponse requestNewRedirectUrl(WithSignerUrl url) {
        // TODO: Er det noe annet å bruke i stede for?
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Marshalling.marshal(new XMLDirectSignerUpdateRequest().withRedirectUrl(new XMLEmptyElement()), os);
            var request = HttpRequest.newBuilder()
                    .uri(url.getSignerUrl())
                    .header(ACCEPT, APPLICATION_XML_TYPE.getType())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(os.toByteArray()))
                    .build();

            var result = httpClient.httpClient().send(request, BodyHandlers.ofInputStream());
            return parseResponse(result, XMLDirectSignerResponse.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public XMLDirectSignatureJobStatusResponse sendSignatureJobStatusRequest(final URI statusUrl) {
        return call(() -> {
            var request = HttpRequest.newBuilder()
                    .uri(statusUrl)
                    .header(ACCEPT, APPLICATION_XML_TYPE.getType())
                    .GET()
                    .build();
            try {
                var result = httpClient.httpClient().send(request, BodyHandlers.ofInputStream());
                ResponseStatus.resolve(result.statusCode()).expect(SUCCESSFUL).orThrow(status -> {
                    if (status == FORBIDDEN) {
                        XMLError error = extractError(result);
                        if (ErrorCodes.INVALID_STATUS_QUERY_TOKEN.sameAs(error.getErrorCode())) {
                            return new InvalidStatusQueryTokenException(statusUrl, error.getErrorMessage());
                        }
                    } else if (status == NOT_FOUND) {
                        XMLError error = extractError(result);
                        if (SIGNING_CEREMONY_NOT_COMPLETED.sameAs(error.getErrorCode())) {
                            return new CantQueryStatusException(status, error.getErrorMessage());
                        }
                    }
                    return exceptionForGeneralError(result);
                });
                return parseResponse(result, XMLDirectSignatureJobStatusResponse.class);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public ResponseInputStream getDataStream(URI uri, MediaType ... acceptedResponses) {
        return call(() -> {
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET();

            Arrays.stream(acceptedResponses).forEach(mediaType -> {
                requestBuilder.header(ACCEPT, mediaType.getType());
            });

            try {
                HttpResponse<InputStream> response = httpClient.httpClient().send(requestBuilder.build(), BodyHandlers.ofInputStream());
                ResponseStatus.resolve(response.statusCode()).expect(SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response));
                return new ResponseInputStream(
                        response.body(),
                        Integer.parseInt(response.headers()
                            .firstValue(CONTENT_LENGTH)
                            .orElseThrow(() -> new RuntimeException("Expected header " + CONTENT_LENGTH + " to exist"))));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void cancel(final Cancellable cancellable) {
        call(() -> {
            if (cancellable.getCancellationUrl() != null) {
                var response = postEmptyEntity(cancellable.getCancellationUrl().getUrl());
                ResponseStatus.resolve(response.statusCode())
                        .throwIf(CONFLICT, status -> new JobCannotBeCancelledException(status, extractError(response)))
                        .expect(SUCCESSFUL)
                        .orThrow(status -> exceptionForGeneralError(response));
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
            var request = HttpRequest.newBuilder()
                    .uri(UriBuilder.fromUri(httpClient.signatureServiceRoot()).path(target.path(actualSender)).queryParam(POLLING_QUEUE_QUERY_PARAMETER, actualSender.getPollingQueue().value).build())
                    .header(ACCEPT, APPLICATION_XML_TYPE.getType())
                    .GET()
                    .build();
            try {
                HttpResponse<InputStream> response = httpClient.httpClient().send(request, BodyHandlers.ofInputStream());
                StatusType status = ResponseStatus.resolve(response.statusCode())
                        .throwIf(TOO_MANY_REQUESTS, s -> new TooEagerPollingException())
                        .expect(SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response));
                return new JobStatusResponse<>(status == NO_CONTENT ? null : Marshalling.unmarshal(response.body(), responseClass), getNextPermittedPollTime(response));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static Instant getNextPermittedPollTime(HttpResponse<?> response) {
        return response.headers().firstValue(NEXT_PERMITTED_POLL_TIME_HEADER)
                .map(nextPermittedPollTime -> ZonedDateTime.parse(nextPermittedPollTime, ISO_DATE_TIME).toInstant())
                .orElseThrow(() -> new RuntimeException("Expected header " + NEXT_PERMITTED_POLL_TIME_HEADER + " to exist"));
    }

    public void confirm(final Confirmable confirmable) {
        call(() -> {
            if (confirmable.getConfirmationReference() != null) {
                URI url = confirmable.getConfirmationReference().getConfirmationUrl();
                LOG.info("Sends confirmation for '{}' to URL {}", confirmable, url);
                var response = postEmptyEntity(url);
                ResponseStatus.resolve(response.statusCode()).expect(SUCCESSFUL).orThrow(status -> exceptionForGeneralError(response));
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
                var response = delete(url);
                ResponseStatus.resolve(response.statusCode()).expect(SUCCESSFUL).orThrow(status -> exceptionForGeneralError(response));
            } else {
                throw new DocumentsNotDeletableException();
            }
        });
    }

    private HttpResponse<InputStream> postEmptyEntity(URI uri) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header(ACCEPT, APPLICATION_XML_TYPE.getType())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            return httpClient.httpClient().send(request, BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<InputStream> delete(URI uri) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header(ACCEPT, APPLICATION_XML_TYPE.getType())
                    .DELETE()
                    .build();

            return httpClient.httpClient().send(request, BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T parseResponse(HttpResponse<InputStream> response, Class<T> responseType) {
        ResponseStatus.resolve(response.statusCode()).expect(SUCCESSFUL).orThrow(unexpectedStatus -> exceptionForGeneralError(response));
        try(var body = response.body()) {
            return Marshalling.unmarshal(body, responseType);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not parse response.", e);
        }
    }

    private static SignatureException exceptionForGeneralError(HttpResponse<InputStream> response) {
        XMLError error = extractError(response);
        if (BROKER_NOT_AUTHORIZED.sameAs(error.getErrorCode())) {
            return new BrokerNotAuthorizedException(error);
        }
        return new UnexpectedResponseException(error, ResponseStatus.resolve(response.statusCode()).get(), OK);
    }

    private static XMLError extractError(HttpResponse<InputStream> response) {
        XMLError error;
        Optional<String> responseContentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE);
        if (responseContentType.isPresent() && MediaType.valueOf(responseContentType.get()).equals(APPLICATION_XML_TYPE)) {
            try(var body = response.body()) {
                error = Marshalling.unmarshal(body, XMLError.class);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not extract error from body.", e);
            }
        } else {
            String errorAsString;
            try(var body = response.body()) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                for (int length; (length = body.read(buffer)) != -1; ) {
                    result.write(buffer, 0, length);
                }
                errorAsString = result.toString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                // TODO: Kan tolke dette som en empty errorAsString? Dvs ignorere feilen?
                throw new UncheckedIOException("Could not read body as string.", e);
            }
            throw new UnexpectedResponseException(
                    HttpHeaders.CONTENT_TYPE + " " + responseContentType.orElse("unknown") + ": " +
                    Optional.ofNullable(errorAsString).filter(StringUtils::isNoneBlank).orElse("<no content in response>"),
                    ResponseStatus.resolve(response.statusCode()).get(), OK);
        }
        // TODO: Dette skjer vel bare hvis vi ignorere IOExceptions?
        if (error == null) {
            throw new UnexpectedResponseException(null, ResponseStatus.resolve(response.statusCode()).get(), OK);
        }
        return error;
    }
    
}
