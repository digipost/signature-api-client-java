package no.digipost.signature.client.security;

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class BrokerIdTest {

    @Test
    void retainsTheGivenId() {
        assertThat(BrokerId.of("555444").value(), is("555444"));
    }

    @Test
    void requiresAnId() {
        assertThrows(NullPointerException.class, () -> BrokerId.of(null));
    }

    @Test
    void rejectsABlankId() {
        assertThat(assertThrows(ConfigurationException.class, () -> BrokerId.of("")),
                where(Throwable::getMessage, containsString("must not be blank")));
        assertThrows(ConfigurationException.class, () -> BrokerId.of("   "));
    }

    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(BrokerId.class).verify();
    }

}
