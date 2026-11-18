package no.digipost.signature.client;

import no.digipost.signature.client.ServiceEnvironment.OAuthDefaults;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signature.client.security.oauth.Resource;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.stream.Stream;

import static no.digipost.signature.client.ServiceEnvironment.DIFIQA;
import static no.digipost.signature.client.ServiceEnvironment.DIFITEST;
import static no.digipost.signature.client.ServiceEnvironment.PRODUCTION;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceEnvironmentTest {

    @Test
    @SuppressWarnings("deprecation")
    void difiQaIsDeprecatedAndDefunct() {
        assertThrows(ConfigurationException.class, DIFIQA::oAuth);
    }

    @Test
    void stagingAndDifitestHasSamePresetOAuthDefaults() {
        assertThat(STAGING.oAuth(), is(DIFITEST.oAuth()));
    }

    @Test
    void oAuthUsesItsOwnDomainForTheApi() {
        assertAll(Stream.of(PRODUCTION, STAGING).map(env -> () ->
                assertThat(env.oAuth().signatureServiceRootUrl().getHost(), not(env.signatureServiceRootUrl().getHost()))));
    }

    @Test
    void presetOAuthResourceIsTheRootOfTheApiWithoutPath() {
        assertAll(Stream.of(OAuthDefaults.PRODUCTION, OAuthDefaults.STAGING).map(oAuth -> () -> {
            URI api = oAuth.signatureServiceRootUrl();
            assertThat(oAuth.resource(), is(new Resource(api.getScheme() + "://" + api.getAuthority())));
        }));
    }

    @Test
    void aCustomServiceUrlOnlyAppliesWhenAuthenticatingWithTheOrganizationCertificate() {
        URI customUrl = URI.create("https://signering.example.com/api");
        ServiceEnvironment custom = STAGING.withServiceUrl(customUrl);

        assertThat(custom.signatureServiceRootUrl(), is(customUrl));
        assertThat(custom.oAuth(), is(STAGING.oAuth()));
    }

    @Test
    void aTokenEndpointCanBeChangedKeepingTheRestOfTheOAuthConfiguration() {
        URI tokenEndpoint = URI.create("https://idp.example.com/token");
        ServiceEnvironment custom = STAGING.withTokenEndpoint(tokenEndpoint);

        assertThat(custom.oAuth().tokenEndpoint(), is(tokenEndpoint));
        assertThat(custom.oAuth().signatureServiceRootUrl(), is(STAGING.oAuth().signatureServiceRootUrl()));
        assertThat(custom.oAuth().resource(), is(STAGING.oAuth().resource()));
        assertThat(custom.signatureServiceRootUrl(), is(STAGING.signatureServiceRootUrl()));
    }

    @Test
    @SuppressWarnings("deprecation")
    void aCustomEnvironmentWithoutOAuthCanBeGivenAnOAuthConfiguration() {
        ServiceEnvironment custom = new ServiceEnvironment("Custom", URI.create("https://signering.example.com/api"), STAGING.certificatePaths());
        assertThrows(ConfigurationException.class, () -> custom.withTokenEndpoint(URI.create("https://idp.example.com/token")));

        ServiceEnvironment withOAuth = custom.withOAuth(
                URI.create("https://oauth.signering.example.com/api"), URI.create("https://idp.example.com/token"), new Resource("https://oauth.signering.example.com"));

        assertThat(withOAuth.oAuth().signatureServiceRootUrl(), is(URI.create("https://oauth.signering.example.com/api")));
        assertThat(withOAuth.oAuth().tokenEndpoint(), is(URI.create("https://idp.example.com/token")));
        assertThat(withOAuth.oAuth().resource(), is(new Resource("https://oauth.signering.example.com")));
        assertThat(withOAuth.signatureServiceRootUrl(), is(URI.create("https://signering.example.com/api")));
    }

}
