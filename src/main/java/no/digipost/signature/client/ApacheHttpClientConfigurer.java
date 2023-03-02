package no.digipost.signature.client;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ApacheHttpClientConfigurer {

    Consumer<Object> NO_CHANGES = t -> {};

    default ApacheHttpClientConfigurer configureSockets(BiConsumer<? super SocketConfig.Builder, Timeout> timeoutSetter, Duration timeoutDuration) {
        return configure(config -> timeoutSetter.accept(config, Timeout.of(timeoutDuration)), NO_CHANGES, NO_CHANGES);
    }

    default ApacheHttpClientConfigurer configureConnections(BiConsumer<? super ConnectionConfig.Builder, Timeout> timeoutSetter, Duration timeoutDuration) {
        return configure(NO_CHANGES, config -> timeoutSetter.accept(config, Timeout.of(timeoutDuration)), NO_CHANGES);
    }

    default ApacheHttpClientConfigurer configureRequests(BiConsumer<? super RequestConfig.Builder, Timeout> timeoutSetter, Duration timeoutDuration) {
        return configure(NO_CHANGES, NO_CHANGES, config -> timeoutSetter.accept(config, Timeout.of(timeoutDuration)));
    }

    ApacheHttpClientConfigurer configure(
            Consumer<? super SocketConfig.Builder> socketConfig,
            Consumer<? super ConnectionConfig.Builder> connectionConfig,
            Consumer<? super RequestConfig.Builder> requestConfig);


    ApacheHttpClientConfigurer configure(
            Consumer<? super HttpClientBuilder> httpClientCustomizer);

}
