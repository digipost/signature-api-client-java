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
package no.digipost.signature.client.core.internal.http;

import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.generator.InRange;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import no.digipost.signature.client.core.internal.http.ResponseStatus.Custom;
import no.digipost.signature.client.core.internal.http.ResponseStatus.Unknown;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response.Status;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class ResponseStatusTest {

    @Test
    public void resolveStandardHttpStatus() {
        assertThat(ResponseStatus.resolve(200), is(Status.OK));
    }

    @Test
    public void resolveCustomHttpStatus() {
        assertThat(ResponseStatus.resolve(422), is(Custom.UNPROCESSABLE_ENTITY));
    }

    @Test
    public void resolveUnknownHttpStatus() {
        assertThat(ResponseStatus.resolve(478), is(ResponseStatus.unknown(478)));
    }

    @Theory
    public void correctEqualsHashCodeForAnyResolvedStatus(@ForAll @InRange(minInt=0, maxInt=1000) int anyStatusCode) {
        assertThat(ResponseStatus.resolve(anyStatusCode), is(ResponseStatus.resolve(anyStatusCode)));
        assertThat(ResponseStatus.resolve(anyStatusCode), not(ResponseStatus.resolve(anyStatusCode + 1)));
    }

    @Test
    public void correctEqualsHashCodeForUnknownStatus() {
        EqualsVerifier.forClass(Unknown.class).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }
}
