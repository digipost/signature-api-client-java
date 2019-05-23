package no.digipost.signature.client.direct;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class SignerStatusTest {

    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(SignerStatus.class).verify();
    }

}
