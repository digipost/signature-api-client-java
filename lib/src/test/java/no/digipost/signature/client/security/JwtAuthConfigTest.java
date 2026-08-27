package no.digipost.signature.client.security;

import no.digipost.signature.client.core.exceptions.ConfigurationException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class JwtAuthConfigTest {

    @Test
    void retainsTheConfiguredClientId() {
        assertThat(JwtAuthConfig.forClient("my-client-id").clientId, is("my-client-id"));
    }

    @Test
    void requiresAClientId() {
        assertThrows(NullPointerException.class, () -> JwtAuthConfig.forClient(null));
    }

    @Test
    void rejectsABlankClientId() {
        assertThat(assertThrows(ConfigurationException.class, () -> JwtAuthConfig.forClient("")),
                where(Throwable::getMessage, containsString("must not be blank")));
        assertThrows(ConfigurationException.class, () -> JwtAuthConfig.forClient("   "));
    }

    @Test
    void describesItselfWithoutRevealingAnythingSensitive() {
        assertThat(JwtAuthConfig.forClient("my-client-id").toString(), containsString("my-client-id"));
    }

}
