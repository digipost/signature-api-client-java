package no.digipost.signature.client.direct;

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.signature.api.xml.XMLDirectSignerStatusValue;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static co.unruly.matchers.Java8Matchers.where;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static no.digipost.DiggExceptions.mayThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;

class SignerStatusTest {

    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(SignerStatus.class).verify();
    }

    @Test
    void constructingFromJaxbInstanceYieldsSameInstance() {
        Stream<SignerStatus> knownStatuses = Stream.of(SignerStatus.class.getDeclaredFields())
                .filter(f -> f.getType() == SignerStatus.class)
                .filter(f -> isPublic(f.getModifiers()) && isStatic(f.getModifiers()))
                .map(mayThrow((Field constantField) -> (SignerStatus) constantField.get(SignerStatus.class)).asUnchecked());

        assertAll(knownStatuses.map(status ->
                () -> assertThat(XMLDirectSignerStatusValue.of(status.getIdentifier()), where(SignerStatus::of, sameInstance(status)))));
    }

}
