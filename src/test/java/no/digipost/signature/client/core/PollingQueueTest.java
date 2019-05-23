package no.digipost.signature.client.core;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PollingQueueTest {

    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(PollingQueue.class).verify();
    }

}
