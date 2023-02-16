package no.digipost.signature.client.core.internal.http;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class StatusCodeTest {

    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(StatusCode.class).verify();
    }

}
