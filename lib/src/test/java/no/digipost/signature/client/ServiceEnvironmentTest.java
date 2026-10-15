package no.digipost.signature.client;

import no.digipost.signature.client.core.exceptions.ConfigurationException;
import org.junit.jupiter.api.Test;

import static no.digipost.signature.client.ServiceEnvironment.DIFIQA;
import static no.digipost.signature.client.ServiceEnvironment.DIFITEST;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

}
