package no.digipost.signature.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.digipost.signature.client.ClientConfiguration.MANDATORY_USER_AGENT;
import static no.digipost.signature.client.ClientMetadata.VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class ClientConfigurationTest {

    private ClientConfiguration.Builder config;

    @BeforeEach
    public void instantiateConfigBuilder() {
        config = ClientConfiguration.builder(TestKonfigurasjon.CLIENT_KEYSTORE);
    }

    @Test
    public void givesDefaultUserAgent() {
        assertThat(config.createUserAgentString(), both(is(MANDATORY_USER_AGENT)).and(containsString(VERSION)));
    }

    @Test
    public void appendsCustomUserAgentAfterDefault() {
        assertThat(config.includeInUserAgent("My Corporation").createUserAgentString(),
                both(startsWith(MANDATORY_USER_AGENT))
                .and(containsString(VERSION))
                .and(containsString("My Corporation")));
    }
}
