package no.digipost.signature.client.portal;

import no.digipost.signature.api.xml.XMLEmail;
import no.digipost.signature.api.xml.XMLNotifications;
import no.digipost.signature.api.xml.XMLSms;
import no.digipost.signature.client.portal.Signature.Signer;
import org.junit.Test;

import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByEmailAddress;
import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByEmailAddressAndMobileNumber;
import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByMobileNumber;
import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByPersonalIdentificationNumber;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignerTest {

    @Test
    public void all_kinds_of_signers_can_be_identified_by_a_signer_identifier() {
        Signer pinSigner = new Signer("00000000000", null);
        Signer emailSigner = new Signer(null, new XMLNotifications(new XMLEmail("email@example.com"), null));
        Signer smsSigner = new Signer(null, new XMLNotifications(null, new XMLSms("11111111")));
        Signer emailAndSmsSigner = new Signer(null, new XMLNotifications(new XMLEmail("email@example.com"), new XMLSms("11111111")));

        assertTrue(pinSigner.isSameAs(identifiedByPersonalIdentificationNumber("00000000000")));
        assertFalse(pinSigner.isSameAs(identifiedByPersonalIdentificationNumber("11111111111")));
        assertFalse(pinSigner.isSameAs(identifiedByEmailAddress("test@example.com")));

        assertTrue(emailSigner.isSameAs(identifiedByEmailAddress("email@example.com")));
        assertFalse(emailSigner.isSameAs(identifiedByEmailAddress("other@example.com")));
        assertFalse(emailSigner.isSameAs(identifiedByEmailAddressAndMobileNumber("email@example.com", "11111111")));

        assertTrue(smsSigner.isSameAs(identifiedByMobileNumber("11111111")));
        assertFalse(smsSigner.isSameAs(identifiedByMobileNumber("22222222")));
        assertFalse(smsSigner.isSameAs(identifiedByEmailAddressAndMobileNumber("email@example.com", "11111111")));

        assertTrue(emailAndSmsSigner.isSameAs(identifiedByEmailAddressAndMobileNumber("email@example.com", "11111111")));
        assertFalse(emailAndSmsSigner.isSameAs(identifiedByEmailAddressAndMobileNumber("other@example.com", "11111111")));
        assertFalse(emailAndSmsSigner.isSameAs(identifiedByEmailAddressAndMobileNumber("email@example.com", "00000000")));
        assertFalse(emailAndSmsSigner.isSameAs(identifiedByEmailAddress("email@example.com")));
        assertFalse(emailAndSmsSigner.isSameAs(identifiedByMobileNumber("11111111")));
    }

}
