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

import no.motif.Singular;
import org.junit.Test;

import static no.digipost.signature.client.portal.PortalSigner.identifiedByEmail;
import static no.digipost.signature.client.portal.PortalSigner.identifiedByEmailAndMobileNumber;
import static no.digipost.signature.client.portal.PortalSigner.identifiedByMobileNumber;
import static no.digipost.signature.client.portal.PortalSigner.identifiedByPersonalIdentificationNumber;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PortalSignerTest {

    @Test
    public void get_personal_identification_number() {

        PortalSigner portalSigner = identifiedByPersonalIdentificationNumber(
                "01013300001",
                Notifications.builder().withEmailTo("email@example.com").build()
        ).build();

        assertThat(portalSigner.getIdentifier().get(), is("01013300001"));
        assertTrue(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }

    @Test
    public void get_email_custom_identifier() {
        PortalSigner portalSigner = identifiedByEmail("email@example.com").build();

        assertThat(portalSigner.getIdentifier(), is(Singular.<String>none()));
        assertThat(portalSigner.getNotifications().getEmailAddress(), is("email@example.com"));
        assertTrue(portalSigner.getNotifications().shouldSendEmail());
        assertFalse(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }

    @Test
    public void get_mobile_number_custom_identifier() {
        PortalSigner portalSigner = identifiedByMobileNumber("12345678").build();

        assertThat(portalSigner.getIdentifier(), is(Singular.<String>none()));
        assertThat(portalSigner.getNotifications().getMobileNumber(), is("12345678"));
        assertTrue(portalSigner.getNotifications().shouldSendSms());
        assertFalse(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }

    @Test
    public void get_email_and_mobile_number_custom_identifier() {
        PortalSigner portalSigner = identifiedByEmailAndMobileNumber("email@example.com", "12345678").build();

        assertThat(portalSigner.getIdentifier(), is(Singular.<String>none()));

        assertThat(portalSigner.getNotifications().getEmailAddress(), is("email@example.com"));
        assertTrue(portalSigner.getNotifications().shouldSendEmail());

        assertThat(portalSigner.getNotifications().getMobileNumber(), is("12345678"));
        assertTrue(portalSigner.getNotifications().shouldSendSms());

        assertFalse(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }
}
