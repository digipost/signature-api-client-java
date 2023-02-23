package no.digipost.signature.client.core.internal.configuration;

import no.digipost.signature.client.ApacheHttpClientConfigurer;
import no.digipost.signature.client.TimeoutsConfigurer;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;

import java.time.Duration;
import java.util.function.Consumer;

public final class ApacheHttpClientBuilderConfigurer implements TimeoutsConfigurer, ApacheHttpClientConfigurer, Configurer<HttpClientBuilder> {

    private final SocketConfig.Builder socketConfig = SocketConfig.custom();
    private final ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom();
    private final RequestConfig.Builder requestConfig = RequestConfig.custom();

    private Configurer<PoolingHttpClientConnectionManagerBuilder> connectionManagerConfigurer = connectionManager -> connectionManager
            .setDefaultSocketConfig(socketConfig.build())
            .setDefaultConnectionConfig(connectionConfig.build());

    private Configurer<HttpClientBuilder> httpClientConfigurer = httpClient -> httpClient
            .setDefaultRequestConfig(requestConfig.build());


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
        this.httpClientConfigurer = httpClientConfigurer.andThen(httpClientCustomizer::accept);
        return this;
    }


    public ApacheHttpClientBuilderConfigurer configureSsl(Configurer<? super SSLConnectionSocketFactoryBuilder> sslConfig) {
        this.connectionManagerConfigurer = connectionManagerConfigurer.andThen(connectionManager -> {
            SSLConnectionSocketFactoryBuilder sslSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create();
            sslConfig.applyTo(sslSocketFactoryBuilder);
            connectionManager.setSSLSocketFactory(sslSocketFactoryBuilder.build());
        });
        return this;
    }


    @Override
    public void applyTo(HttpClientBuilder httpClientBuilder) {
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        connectionManagerConfigurer.applyTo(connectionManagerBuilder);

        httpClientConfigurer
            .andThen(httpClient -> httpClient.setConnectionManager(connectionManagerBuilder.build()))
            .applyTo(httpClientBuilder);
    }

}
