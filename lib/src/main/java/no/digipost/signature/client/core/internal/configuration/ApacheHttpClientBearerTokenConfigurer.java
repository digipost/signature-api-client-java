package no.digipost.signature.client.core.internal.configuration;

import no.digipost.signature.client.core.internal.http.MutualTlsTokenProvider;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.util.logging.Logger;

import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

/**
 * Makes the client send an {@code Authorization: Bearer <token>} header on every request, using an
 * access token acquired from the configured OAuth 2.0 token endpoint.
 *
 * <p>Also recovers from a token being rejected. A token can stop working before it is considered
 * stale by the {@link MutualTlsTokenProvider}, for instance if it is revoked, if the token endpoint
 * is restarted, or if this host's clock runs ahead of the token endpoint's. Without this, every
 * subsequent request would keep failing until the cached token expired on its own.
 *
 * <p>A {@code 401} which turns out to signal a permanent authorization problem rather than a
 * rejected token will therefore cost one extra attempt for safe requests before the failure is
 * passed on to the caller. That is a deliberate trade-off, and is bounded to a single retry.
 *
 * <p>This class is not part of the public API of this library and may change without notice.
 */
public final class ApacheHttpClientBearerTokenConfigurer implements Configurer<HttpClientBuilder> {

    /**
     * The {@link HttpContext} attribute holding the access token which was put on the request, so
     * that it can be identified as the rejected one if the response turns out to be a 401.
     */
    static final String APPLIED_ACCESS_TOKEN = "no.digipost.signature.client.applied-access-token";

    private static final String RECOVERY_EXEC_NAME = "bearer-token-recovery";

    private final MutualTlsTokenProvider tokenProvider;

    public ApacheHttpClientBearerTokenConfigurer(MutualTlsTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void applyTo(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder
                .addRequestInterceptorLast(new RequestBearerTokenInterceptor(tokenProvider))
                // Placed outside the protocol chain element, which is what runs the request
                // interceptors. A retry from here therefore runs the interceptor above again, which
                // puts a freshly acquired token on the retried request.
                .addExecInterceptorBefore(
                        ChainElement.PROTOCOL.name(), RECOVERY_EXEC_NAME, new RejectedTokenRecoveryExec(tokenProvider));
    }


    private static final class RequestBearerTokenInterceptor implements HttpRequestInterceptor {

        private final MutualTlsTokenProvider tokenProvider;

        RequestBearerTokenInterceptor(MutualTlsTokenProvider tokenProvider) {
            this.tokenProvider = tokenProvider;
        }

        @Override
        public void process(HttpRequest request, EntityDetails entityDetails, HttpContext context) {
            String accessToken = tokenProvider.getToken();
            request.setHeader(AUTHORIZATION, "Bearer " + accessToken);
            context.setAttribute(APPLIED_ACCESS_TOKEN, accessToken);
        }
    }


    private static final class RejectedTokenRecoveryExec implements ExecChainHandler {

        private static final Logger LOG = Logger.getLogger(RejectedTokenRecoveryExec.class.getName());

        private final MutualTlsTokenProvider tokenProvider;

        RejectedTokenRecoveryExec(MutualTlsTokenProvider tokenProvider) {
            this.tokenProvider = tokenProvider;
        }

        @Override
        public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain)
                throws IOException, HttpException {

            ClassicHttpResponse response = chain.proceed(request, scope);
            if (response.getCode() != HttpStatus.SC_UNAUTHORIZED) {
                return response;
            }

            Object appliedAccessToken = scope.clientContext.getAttribute(APPLIED_ACCESS_TOKEN);
            if (appliedAccessToken instanceof String) {
                tokenProvider.invalidate((String) appliedAccessToken);
            }

            if (!canBeSafelyRetried(request)) {
                // The token has still been discarded above, so the next request will acquire a new
                // one. Only this response is passed on to the caller as the failure it is.
                return response;
            }

            // The connection must be released before it can be used for the retry.
            EntityUtils.consume(response.getEntity());
            response.close();

            LOG.fine(() -> "Retrying " + request.getMethod() + " " + request.getPath() +
                    " once with a new access token, as the one used was rejected");

            // Retried from a pristine copy of the original request, the same way the http client's own
            // retry handling does it. The request just attempted has had protocol headers such as
            // Content-Length or Transfer-Encoding added to it, and those cannot be applied a second time.
            return chain.proceed(ClassicRequestBuilder.copy(scope.originalRequest).build(), scope);
        }

        /**
         * Only requests which are both safe to repeat from the API's point of view, and physically
         * repeatable, are retried. Notably this excludes creating signature jobs: it is not safe to
         * send such a request twice, and its multipart body is not repeatable anyway.
         */
        private static boolean canBeSafelyRetried(ClassicHttpRequest request) {
            HttpEntity entity = request.getEntity();
            if (entity != null && !entity.isRepeatable()) {
                return false;
            }
            return Method.isSafe(request.getMethod());
        }
    }

}
