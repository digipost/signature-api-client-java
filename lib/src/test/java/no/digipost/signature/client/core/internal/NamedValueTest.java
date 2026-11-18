package no.digipost.signature.client.core.internal;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class NamedValueTest {

    static final class BestNumber extends NamedValue<Long> {
        public BestNumber(Long value) {
            super("best number", value);
        }
    }

    @Test
    void correctEqualAndHashCodeInSubClass() {
        EqualsVerifier.forClass(BestNumber.class)
            .usingGetClass()
            .withIgnoredFields("description")
            .verify();
    }

    @Test
    void baseClassHasCorrectEqualAndHashCode() {
        EqualsVerifier.forClass(NamedValue.class)
            .usingGetClass()
            .withIgnoredFields("description")
            .verify();
    }

    @Test
    void doesNotAllowNullName() {
        assertThrows(NullPointerException.class, () -> new NamedValue<String>(null, "value") {});
    }

    @Test
    void doesNotAllowNullValue() {
        assertThrows(NullPointerException.class, () -> new NamedValue<Void>("name", null) {});
    }

    @Test
    void toStringIncludesNameAndDefaultStringRepresentationOfValue() {
        qt()
            .forAll(strings().allPossible().ofLengthBetween(1, 20), integers().all().map(Instant::ofEpochMilli))
            .as((name, value) -> new NamedValue<Instant>(name, value) {})
            .checkAssert(namedValue -> assertAll(
                    () -> assertThat(namedValue, where(NamedValue::toString, containsString(namedValue.name()))),
                    () -> assertThat(namedValue, where(NamedValue::toString, containsString(Objects.toString(namedValue.value()))))));


        BestNumber fortyTwo = new BestNumber(42L);
        assertAll(
                () -> assertThat(fortyTwo, where(BestNumber::toString, containsString("best number"))),
                () -> assertThat(fortyTwo, where(BestNumber::toString, containsString("42"))));
    }

    @Test
    void nameAndValueAsConstructed() {
        BestNumber fortyTwo = new BestNumber(42L);
        assertAll(
                () -> assertThat(fortyTwo, where(BestNumber::toString, containsString("best number"))),
                () -> assertThat(fortyTwo, where(BestNumber::toString, containsString("42"))));
    }

    @Test
    void toStringIncludesCustomDescriptionForValue() {
        NamedValue<String> pincode =  new NamedValue<String>("pincode", "1234", "****") { };
        assertAll(
                () -> assertThat(pincode, where(NamedValue::toString, containsString("****"))),
                () -> assertThat(pincode, where(NamedValue::toString, not(containsString("1234")))),
                () -> assertThat(pincode, where(NamedValue::value, is("1234"))));
    }

}
