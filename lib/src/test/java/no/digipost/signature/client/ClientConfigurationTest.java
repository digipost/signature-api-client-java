package no.digipost.signature.client;

import org.junit.jupiter.api.Test;

import static no.digipost.signature.client.ClientConfiguration.MANDATORY_USER_AGENT;
import static no.digipost.signature.client.ClientConfiguration.ClientMetadata.VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;

class ClientConfigurationTest {

    private final ClientConfiguration.Builder config = ClientConfiguration.builder(TestKonfigurasjon.CLIENT_KEYSTORE);

    @Test
    void givesDefaultUserAgent() {
        assertThat(config.userAgentConfigurer.createUserAgentString(),
                contains(both(is(MANDATORY_USER_AGENT)).and(containsString(VERSION))));
    }

    @Test
    void appendsCustomUserAgentAfterDefault() {
        assertThat(config.includeInUserAgent("My Corporation").userAgentConfigurer.createUserAgentString(),
                contains(both(startsWith(MANDATORY_USER_AGENT))
                .and(containsString(VERSION))
                .and(containsString("My Corporation"))));
    }
}
