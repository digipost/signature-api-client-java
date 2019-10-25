package no.digipost.signature.client.core;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class SenderTest {

    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(Sender.class).verify();
    }

}
