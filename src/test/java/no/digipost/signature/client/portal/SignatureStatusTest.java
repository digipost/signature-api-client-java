package no.digipost.signature.client.portal;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class SignatureStatusTest {

    @Test
    public void equals_and_hashCode() {
        EqualsVerifier.forClass(SignatureStatus.class).verify();
    }

}