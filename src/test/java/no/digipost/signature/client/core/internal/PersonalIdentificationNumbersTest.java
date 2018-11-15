package no.digipost.signature.client.core.internal;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class PersonalIdentificationNumbersTest {

    @Property
    public void maskingNeverThrowsException(String randomString) {
        assertThat(mask(randomString), isA(String.class));
    }

    @Property
    public void alwaysReturnsStringWithSameLengthAsGiven(String randomString) {
        assertThat(mask(randomString).length(), is(randomString.length()));
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
