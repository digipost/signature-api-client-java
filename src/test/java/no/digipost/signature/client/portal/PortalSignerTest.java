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
package no.digipost.signature.client.portal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PortalSignerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void get_personal_identification_number() {
        String expected = "01013300001";

        PortalSigner portalSigner = PortalSigner.builder(
                expected,
                Notifications.builder().withEmailTo("email@example.com").build())
                .build();

        String actual = portalSigner.getPersonalIdentificationNumber();
        assertThat(actual, is(expected));
        assertTrue(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }

    @Test
    public void get_personal_identification_number_throws_if_custom_identifier_set() {
        expectedException.expect(IllegalStateException.class);

        PortalSigner portalSigner = PortalSigner.withCustomIdentifier("email@example.com").build();

        portalSigner.getPersonalIdentificationNumber();
    }

    @Test
    public void get_custom_identifier() {
        String expected = "email@example.com";

        PortalSigner portalSigner = PortalSigner.withCustomIdentifier(expected).build();

        String actual = portalSigner.getCustomIdentifier();
        assertThat(actual, is(expected));
        assertFalse(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }

    @Test
    public void get_custom_identifier_throws_if_personal_identification_number_set() {
        expectedException.expect(IllegalStateException.class);

        PortalSigner portalSigner = PortalSigner.builder(
                "01013300001",
                Notifications.builder().withEmailTo("email@example.com").build()).build();

        portalSigner.getCustomIdentifier();
    }
}
