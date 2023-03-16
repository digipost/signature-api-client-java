package no.digipost.signature.client.core.internal;

import org.junit.jupiter.api.Test;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;

public class PersonalIdentificationNumbersTest {

    @Test
    public void maskingNeverThrowsException() {
        qt()
            .forAll(strings().allPossible().ofLengthBetween(0, 100))
            .check(randomString -> mask(randomString) instanceof String);
    }

    @Test
    public void alwaysReturnsStringWithSameLengthAsGiven() {
        qt()
            .forAll(strings().allPossible().ofLengthBetween(0, 100))
            .checkAssert(randomString -> assertThat(mask(randomString).length(), is(randomString.length())));
    }

    @Test
    public void masksTheIdPartOfAPersonalIdentificationNumber() {
        assertThat(mask("24068112345"), is("240681*****"));
    }

    @Test
    public void maskingNullIsNull() {
        assertThat(mask(null), nullValue());
    }

}
