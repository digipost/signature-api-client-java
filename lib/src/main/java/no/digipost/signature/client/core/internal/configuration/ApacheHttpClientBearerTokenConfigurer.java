package no.digipost.signature.client.core.internal.configuration;

import no.digipost.signature.client.core.internal.http.MutualTlsTokenProvider;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.util.logging.Logger;

import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

/**
 * Sends an {@code Authorization: Bearer <token>} header on every request. On a {@code 401}, the token
 * is discarded and the request is retried once with a new one, e.g. if the token was revoked.
 */
public final class ApacheHttpClientBearerTokenConfigurer implements Configurer<HttpClientBuilder> {

    /**
     * The token sent with the request, to know which one to discard on a 401.
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
                // Before PROTOCOL, so a retry runs the interceptor above again and gets a new token
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

            // The connection must be released before it can be used for the retry.
            EntityUtils.consume(response.getEntity());
            response.close();

            LOG.fine(() -> "Retrying " + request.getMethod() + " " + request.getPath() +
                    " once with a new access token, as the one used was rejected");

            // Retry a copy of the original request, as protocol headers have been added to the attempted one
            return chain.proceed(ClassicRequestBuilder.copy(scope.originalRequest).build(), scope);
        }
    }

}
