package no.digipost.signature.client;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static no.digipost.signature.client.ServiceEnvironment.QA;
import static no.digipost.signature.client.ServiceEnvironment.DIFITEST;
import static no.digipost.signature.client.ServiceEnvironment.PRODUCTION;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;
import static uk.co.probablyfine.matchers.OptionalMatchers.empty;

class ServiceEnvironmentTest {

    private static final URI CUSTOM_TOKEN_ENDPOINT = URI.create("https://midp.example.com/oauth2/token");

    @Test
    void thePredefinedEnvironmentsKnowTheirTokenEndpoint() {
        assertThat(PRODUCTION.tokenEndpoint(), contains(URI.create("https://midp.digipost.no/oauth2/token")));
        assertThat(DIFITEST.tokenEndpoint(), contains(URI.create("https://midp.difitest.digipost.no/oauth2/token")));
        assertThat(QA.tokenEndpoint(), contains(URI.create("https://midp.qa.digipost.no/oauth2/token")));
    }

    @Test
    void stagingInheritsTheTokenEndpointOfDifitest() {
        assertThat(STAGING.tokenEndpoint(), is(DIFITEST.tokenEndpoint()));
    }

    /**
     * Every copy method has to carry the token endpoint over, or enabling JWT authentication would
     * silently stop working for anyone customizing their environment.
     */
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
        ServiceEnvironment custom = new ServiceEnvironment(
                "Custom", URI.create("https://localhost:8443/api"), Arrays.asList("some/certificate.cer"));

        assertThat(custom.tokenEndpoint(), empty());
        assertThat(custom.withTokenEndpoint(CUSTOM_TOKEN_ENDPOINT).tokenEndpoint(), contains(CUSTOM_TOKEN_ENDPOINT));
    }

    @Test
    void theTokenEndpointOfAPredefinedEnvironmentCanBeOverridden() {
        assertThat(DIFITEST.withTokenEndpoint(CUSTOM_TOKEN_ENDPOINT).tokenEndpoint(), contains(CUSTOM_TOKEN_ENDPOINT));
        // without affecting the predefined environment itself
        assertThat(DIFITEST.tokenEndpoint(), contains(URI.create("https://midp.difitest.digipost.no/oauth2/token")));
    }

}
