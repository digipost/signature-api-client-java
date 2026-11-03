package no.digipost.signature.client.core.internal.configuration;

import no.digipost.signature.client.ApacheHttpClientConfigurer;
import no.digipost.signature.client.ConnectionPoolConfigurer;
import no.digipost.signature.client.TimeoutsConfigurer;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;

import java.time.Duration;
import java.util.function.Consumer;

public final class ApacheHttpClientBuilderConfigurer implements TimeoutsConfigurer, ConnectionPoolConfigurer, ApacheHttpClientConfigurer, Configurer<HttpClientBuilder> {

    private final PoolingHttpClientConnectionManagerBuilder connectionManagerConfig = PoolingHttpClientConnectionManagerBuilder.create();

    private final SocketConfig.Builder socketConfig = SocketConfig.custom();
    private final ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom();
    private final RequestConfig.Builder requestConfig = RequestConfig.custom();

    private final Configurer<PoolingHttpClientConnectionManagerBuilder> defaultConnectionManagerConfigurer = connectionManager -> connectionManager
            .setDefaultSocketConfig(socketConfig.build())
            .setDefaultConnectionConfig(connectionConfig.build());

    private final Configurer<HttpClientBuilder> defaultRequestConfigurer = httpClient -> httpClient
            .setDefaultRequestConfig(requestConfig.build())
            .setConnectionManager(connectionManagerConfig.build());

    private Configurer<PoolingHttpClientConnectionManagerBuilder> additionalConnectionManagerConfigurer = Configurer.notConfigured();
    private Configurer<HttpClientBuilder> additionalHttpClientConfigurer = Configurer.notConfigured();


    public ApacheHttpClientBuilderConfigurer() {
        maxTotalConnectionsInPool(DEFAULT_TOTAL_CONNECTIONS_IN_POOL);
    }

    public ApacheHttpClientBuilderConfigurer connectionManager(Configurer<PoolingHttpClientConnectionManagerBuilder> connectionManagerConfigurer) {
        this.additionalConnectionManagerConfigurer = additionalConnectionManagerConfigurer.andThen(connectionManagerConfigurer);
        return this;
    }

    @Override
    public ApacheHttpClientBuilderConfigurer socketTimeout(Duration duration) {
        configureSockets(SocketConfig.Builder::setSoTimeout, duration);
        return this;
    }

    @Override
    public ApacheHttpClientBuilderConfigurer connectTimeout(Duration duration) {
        configureConnections(ConnectionConfig.Builder::setConnectTimeout, duration);
        return this;
    }

    @Override
    public ApacheHttpClientBuilderConfigurer connectionRequestTimeout(Duration duration) {
         configureRequests(RequestConfig.Builder::setConnectionRequestTimeout, duration);
         return this;
    }

    @Override
    public ApacheHttpClientBuilderConfigurer responseArrivalTimeout(Duration duration) {
        configureRequests(RequestConfig.Builder::setResponseTimeout, duration);
        return this;
    }

    @Override
    public ApacheHttpClientBuilderConfigurer maxTotalConnectionsInPool(int count) {
        return connectionManager(connectionMgr -> connectionMgr

                // a "route" is essentially the target host (optionally with proxy hops). The HTTP client
                // is specifically for one API, and separating connections per route is not applicable,
                // therefore the per route count is the same as total count of connections
                .setMaxConnPerRoute(count).setMaxConnTotal(count));
    }

    @Override
    public ApacheHttpClientBuilderConfigurer configure(
            Consumer<? super SocketConfig.Builder> socketConfig,
            Consumer<? super ConnectionConfig.Builder> connectionConfig,
            Consumer<? super RequestConfig.Builder> requestConfig) {

        socketConfig.accept(this.socketConfig);
        connectionConfig.accept(this.connectionConfig);
        requestConfig.accept(this.requestConfig);
        return this;
    }

    @Override
    public ApacheHttpClientBuilderConfigurer configure(Consumer<? super HttpClientBuilder> httpClientCustomizer) {
        this.additionalHttpClientConfigurer = additionalHttpClientConfigurer.andThen(Configurer.of(httpClientCustomizer));
        return this;
    }


    @Override
    public void applyTo(HttpClientBuilder httpClientBuilder) {
        defaultConnectionManagerConfigurer
            .andThen(additionalConnectionManagerConfigurer)
            .applyTo(connectionManagerConfig);

        defaultRequestConfigurer
            .andThen(additionalHttpClientConfigurer)
            .applyTo(httpClientBuilder);
    }

}
