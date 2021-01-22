package no.digipost.signature.client.portal;

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.signature.api.xml.XMLPortalSignatureStatusValue;
import no.digipost.signature.client.direct.SignerStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static no.digipost.DiggExceptions.mayThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SignatureStatusTest {

    @Test
    public void equals_and_hashCode() {
        EqualsVerifier.forClass(SignatureStatus.class).verify();
    }

    @Test
    void constructingFromJaxbInstanceYieldsSameInstance() {
        Stream<SignatureStatus> knownStatuses = Stream.of(SignerStatus.class.getDeclaredFields())
                .filter(f -> f.getType() == SignatureStatus.class)
                .filter(f -> isPublic(f.getModifiers()) && isStatic(f.getModifiers()))
                .map(mayThrow((Field constantField) -> (SignatureStatus) constantField.get(SignatureStatus.class)).asUnchecked());

        assertAll(knownStatuses.map(status ->
                () -> assertThat(XMLPortalSignatureStatusValue.of(status.getIdentifier()), where(SignatureStatus::of, sameInstance(status)))));
    }

}
