package no.digipost.signature.client.portal;

import org.junit.jupiter.api.Test;

import static co.unruly.matchers.Java8Matchers.where;
import static co.unruly.matchers.Java8Matchers.whereNot;
import static co.unruly.matchers.OptionalMatchers.contains;
import static co.unruly.matchers.OptionalMatchers.empty;
import static no.digipost.signature.client.portal.PortalSigner.identifiedByEmail;
import static no.digipost.signature.client.portal.PortalSigner.identifiedByEmailAndMobileNumber;
import static no.digipost.signature.client.portal.PortalSigner.identifiedByMobileNumber;
import static no.digipost.signature.client.portal.PortalSigner.identifiedByPersonalIdentificationNumber;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PortalSignerTest {

    @Test
    public void get_personal_identification_number() {

        PortalSigner portalSigner = identifiedByPersonalIdentificationNumber(
                "01013300001",
                Notifications.builder().withEmailTo("email@example.com").build()
        ).build();

        assertThat(portalSigner, where(PortalSigner::getIdentifier, contains("01013300001")));
        assertThat(portalSigner, where(PortalSigner::isIdentifiedByPersonalIdentificationNumber));
    }

    @Test
    public void get_email_custom_identifier() {
        PortalSigner portalSigner = identifiedByEmail("email@example.com").build();

        assertThat(portalSigner, where(PortalSigner::getIdentifier, empty()));
        assertThat(portalSigner.getNotifications(), where(Notifications::getEmailAddress, is("email@example.com")));
        assertThat(portalSigner.getNotifications(), where(Notifications::shouldSendEmail));
        assertThat(portalSigner, whereNot(PortalSigner::isIdentifiedByPersonalIdentificationNumber));
    }

    @Test
    public void get_mobile_number_custom_identifier() {
        PortalSigner portalSigner = identifiedByMobileNumber("12345678").build();

        assertThat(portalSigner.getIdentifier(), is(empty()));
        assertThat(portalSigner.getNotifications(), where(Notifications::getMobileNumber, is("12345678")));
        assertThat(portalSigner.getNotifications(), where(Notifications::shouldSendSms));
        assertThat(portalSigner, whereNot(PortalSigner::isIdentifiedByPersonalIdentificationNumber));
    }

    @Test
    public void get_email_and_mobile_number_custom_identifier() {
        PortalSigner portalSigner = identifiedByEmailAndMobileNumber("email@example.com", "12345678").build();

        assertThat(portalSigner.getIdentifier(), is(empty()));

        assertThat(portalSigner.getNotifications(), where(Notifications::getEmailAddress, is("email@example.com")));
        assertThat(portalSigner.getNotifications(), where(Notifications::shouldSendEmail));

        assertThat(portalSigner.getNotifications(), where(Notifications::getMobileNumber, is("12345678")));
        assertThat(portalSigner.getNotifications(), where(Notifications::shouldSendSms));

        assertThat(portalSigner, whereNot(PortalSigner::isIdentifiedByPersonalIdentificationNumber));
    }
}
