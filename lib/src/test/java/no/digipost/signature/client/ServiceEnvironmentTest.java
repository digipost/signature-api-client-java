package no.digipost.signature.client;

import no.digipost.signature.client.core.exceptions.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static no.digipost.signature.client.ServiceEnvironment.DIFIQA;
import static no.digipost.signature.client.ServiceEnvironment.DIFITEST;
import static no.digipost.signature.client.ServiceEnvironment.PRODUCTION;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceEnvironmentTest {

    private static final URI CUSTOM_TOKEN_ENDPOINT = URI.create("https://midp.example.com/oauth2/token");

    @Test
    void thePredefinedEnvironmentsKnowTheirTokenEndpoint() {
        assertThat(PRODUCTION.tokenEndpoint(), is(URI.create("https://midp.digipost.no/oauth2/token")));
        assertThat(DIFITEST.tokenEndpoint(), is(URI.create("https://midp.difitest.digipost.no/oauth2/token")));
        // environment is deprecated, no tokenUrl for this env

    }

    @Test
    @SuppressWarnings("deprecation")
    void difiQaIsDeprecatedAndDefunct() {
        assertThrows(ConfigurationException.class, DIFIQA::tokenEndpoint);
    }

    @Test
    void stagingInheritsTheTokenEndpointOfDifitest() {
        assertThat(STAGING.tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
    }

    @Test
    void copyingAnEnvironmentRetainsTheTokenEndpoint() {
        assertThat(DIFITEST.withDescription("Other").tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
        assertThat(DIFITEST.withServiceUrl(URI.create("https://localhost:8443")).tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
        assertThat(DIFITEST.withCertificates("some/certificate.cer").tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
        assertThat(DIFITEST.withCertificates(singletonList("some/certificate.cer")).tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
        assertThat(DIFITEST.withAdditionalCertificates("some/certificate.cer").tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
        assertThat(DIFITEST.withAdditionalCertificates(singletonList("some/certificate.cer")).tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
    }

    @Test
    void aCustomEnvironmentHasNoTokenEndpointUntilGivenOne() {
        @SuppressWarnings("deprecation")
        ServiceEnvironment custom = new ServiceEnvironment(
                "Custom", URI.create("https://localhost:8443/api"), Arrays.asList("some/certificate.cer"));

        assertThrows(ConfigurationException.class, custom::tokenEndpoint);
        assertThat(custom.withTokenEndpoint(CUSTOM_TOKEN_ENDPOINT).tokenEndpoint(), is(CUSTOM_TOKEN_ENDPOINT));
    }

    @Test
    void theTokenEndpointOfAPredefinedEnvironmentCanBeOverridden() {
        assertThat(DIFITEST.withTokenEndpoint(CUSTOM_TOKEN_ENDPOINT).tokenEndpoint(), is(CUSTOM_TOKEN_ENDPOINT));
        // without affecting the predefined environment itself
        assertThat(DIFITEST.tokenEndpoint(), is(URI.create("https://midp.difitest.digipost.no/oauth2/token")));
    }

}
