/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client.core.internal;

import com.pholser.junit.quickcheck.ForAll;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class PersonalIdentificationNumbersTest {

    @Theory
    public void maskingNeverThrowsException(@ForAll String randomString) {
        assertThat(mask(randomString), isA(String.class));
    }

    @Theory
    public void alwaysReturnsStringWithSameLengthAsGiven(@ForAll String randomString) {
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
