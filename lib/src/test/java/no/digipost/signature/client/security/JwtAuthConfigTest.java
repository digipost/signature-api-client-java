package no.digipost.signature.client.security;

import no.digipost.signature.client.core.exceptions.ConfigurationException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class JwtAuthConfigTest {

    private static final BrokerId BROKER = BrokerId.of("555444");

    @Test
    void retainsTheConfiguredClientIdAndBroker() {
        JwtAuthConfig config = JwtAuthConfig.forClient("my-client-id", BROKER);

        assertThat(config.clientId, is("my-client-id"));
        assertThat(config.brokerId, is(BROKER));
    }

    @Test
    void requiresAClientId() {
        assertThrows(NullPointerException.class, () -> JwtAuthConfig.forClient(null, BROKER));
    }

    @Test
    void requiresABrokerId() {
        assertThrows(NullPointerException.class, () -> JwtAuthConfig.forClient("my-client-id", null));
    }

    @Test
    void rejectsABlankClientId() {
        assertThat(assertThrows(ConfigurationException.class, () -> JwtAuthConfig.forClient("", BROKER)),
                where(Throwable::getMessage, containsString("must not be blank")));
        assertThrows(ConfigurationException.class, () -> JwtAuthConfig.forClient("   ", BROKER));
    }

    @Test
    void describesItselfWithoutRevealingAnythingSensitive() {
        String description = JwtAuthConfig.forClient("my-client-id", BROKER).toString();

        assertThat(description, containsString("my-client-id"));
        assertThat(description, containsString("555444"));
    }

}
