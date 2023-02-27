package no.digipost.signature.client.core.internal;

import no.digipost.signature.api.xml.XMLError;
import no.digipost.signature.client.core.exceptions.BrokerNotAuthorizedException;
import no.digipost.signature.client.core.exceptions.SignatureException;
import no.digipost.signature.client.core.exceptions.UnexpectedResponseException;
import no.digipost.signature.client.core.internal.http.ResponseStatus;
import no.digipost.signature.client.core.internal.http.StatusCode;
import no.digipost.signature.client.core.internal.xml.Marshalling;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolException;

import javax.net.ssl.SSLHandshakeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

import static no.digipost.signature.client.core.internal.ErrorCodes.BROKER_NOT_AUTHORIZED;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;

class ClientExceptionMapper {

    static SignatureException exceptionForGeneralError(ClassicHttpResponse response) {
        XMLError error = extractError(response);
        if (BROKER_NOT_AUTHORIZED.sameAs(error.getErrorCode())) {
            return new BrokerNotAuthorizedException(error);
        }
        return new UnexpectedResponseException(error, ResponseStatus.fromHttpResponse(response).get(), StatusCode.OK);
    }


    static XMLError extractError(ClassicHttpResponse response) {
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



    static void doWithMappedClientException(Runnable action) {
        doWithMappedClientException(() -> {
            action.run();
            return null;
        });
    }

    static <T> T doWithMappedClientException(Supplier<T> produceResult) {
        try {
            return produceResult.get();
        } catch (RuntimeException e) {
            throw map(e);
        }
    }


    static private RuntimeException map(RuntimeException e) {
        if (e.getCause() instanceof SSLHandshakeException) {
            return new SignatureException(
                    "Unable to perform SSL handshake with remote server. Some possible causes (could be others, see underlying error): \n" +
                    "* A certificate with the wrong KeyUsage was used. The keyUsage should be DigitalSignature\n" +
                    "* Erroneous configuration of the trust store\n" +
                    "* Intermediate network devices interfering with traffic (e.g. proxies)\n" +
                    "* An attacker impersonating the server (man in the middle)." +
                    "* Wrong TLS version. For Java 7, see 'JSSE tuning parameters' at https://blogs.oracle.com/java-platform-group/entry/diagnosing_tls_ssl_and_https " +
                    "for information about enabling the latest TLS versions." +
                    "* Incorrect certificate. If none of the errors above fixes the issue, it may be because wrong certificate is being used. Please see the Posten" +
                    " signering documentation for buying and installing enterprise certificates. \n"
                    , e);
        }

        return e;
    }

}
