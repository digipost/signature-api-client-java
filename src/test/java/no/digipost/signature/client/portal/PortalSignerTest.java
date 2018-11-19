package no.digipost.signature.client.portal;

import org.junit.Test;

import java.util.Optional;

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

        assertThat(portalSigner.getIdentifier(), is(Optional.<String>empty()));
        assertThat(portalSigner.getNotifications().getEmailAddress(), is("email@example.com"));
        assertTrue(portalSigner.getNotifications().shouldSendEmail());
        assertFalse(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }

    @Test
    public void get_mobile_number_custom_identifier() {
        PortalSigner portalSigner = identifiedByMobileNumber("12345678").build();

        assertThat(portalSigner.getIdentifier(), is(Optional.<String>empty()));
        assertThat(portalSigner.getNotifications().getMobileNumber(), is("12345678"));
        assertTrue(portalSigner.getNotifications().shouldSendSms());
        assertFalse(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }

    @Test
    public void get_email_and_mobile_number_custom_identifier() {
        PortalSigner portalSigner = identifiedByEmailAndMobileNumber("email@example.com", "12345678").build();

        assertThat(portalSigner.getIdentifier(), is(Optional.<String>empty()));

        assertThat(portalSigner.getNotifications().getEmailAddress(), is("email@example.com"));
        assertTrue(portalSigner.getNotifications().shouldSendEmail());

        assertThat(portalSigner.getNotifications().getMobileNumber(), is("12345678"));
        assertTrue(portalSigner.getNotifications().shouldSendSms());

        assertFalse(portalSigner.isIdentifiedByPersonalIdentificationNumber());
    }
}
