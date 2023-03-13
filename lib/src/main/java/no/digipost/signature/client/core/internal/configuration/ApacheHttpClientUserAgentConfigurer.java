package no.digipost.signature.client.core.internal.configuration;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

public final class ApacheHttpClientUserAgentConfigurer implements Configurer<HttpClientBuilder> {

    private final List<String> userAgentParts;
    private final String delimiter;

    public ApacheHttpClientUserAgentConfigurer(String userAgentString) {
        this(asList(userAgentString), " ");
    }

    public ApacheHttpClientUserAgentConfigurer(List<String> userAgentParts, String delimiter) {
        this.userAgentParts = new ArrayList<>(userAgentParts);
        this.delimiter = delimiter;
    }

    public void append(String postfix) {
        userAgentParts.add(postfix);
    }

    @Override
    public void applyTo(HttpClientBuilder httpClientBuilder) {
        createUserAgentString().ifPresent(httpClientBuilder::setUserAgent);
    }

    public Optional<String> createUserAgentString() {
        if (userAgentParts.isEmpty()) {
            return Optional.empty();
        }
        Iterator<String> parts = userAgentParts.iterator();
        StringBuilder userAgent = new StringBuilder(parts.next());
        parts.forEachRemaining(part -> userAgent.append(delimiter).append(part));
        return Optional.of(userAgent.toString());
    }

}
