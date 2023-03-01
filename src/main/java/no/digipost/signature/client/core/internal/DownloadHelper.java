package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.exceptions.HttpIOException;
import no.digipost.signature.client.core.internal.http.SignatureServiceRoot;
import no.digipost.signature.client.core.internal.http.StatusCode;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.HeaderGroup;

import java.io.IOException;
import java.net.URI;

import static no.digipost.signature.client.core.internal.ClientExceptionMapper.doWithMappedClientException;
import static no.digipost.signature.client.core.internal.ClientExceptionMapper.exceptionForGeneralError;
import static no.digipost.signature.client.core.internal.http.StatusCode.Family.SUCCESSFUL;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;

public class DownloadHelper {

    private SignatureServiceRoot serviceRoot;
    private HttpClient httpClient;

    public DownloadHelper(SignatureServiceRoot serviceRoot, HttpClient httpClient) {
        this.serviceRoot = serviceRoot;
        this.httpClient = httpClient;
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

        return doWithMappedClientException(() -> {
            ClassicHttpResponse response = null;
            try {
                try {
                    response = httpClient.executeOpen(null, request, null);
                } catch (IOException e) {
                    throw new HttpIOException(request, e);
                }
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
}
