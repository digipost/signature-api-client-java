package no.digipost.signature.client.core.internal.configuration;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;

public final class ApacheHttpClientProxyConfigurer implements Configurer<HttpClientBuilder> {

    private final HttpHost proxyHost;

    public ApacheHttpClientProxyConfigurer(HttpHost proxyHost) {
        this.proxyHost = proxyHost;
    }

    @Override
    public void applyTo(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder.setProxy(proxyHost);
    }

}
