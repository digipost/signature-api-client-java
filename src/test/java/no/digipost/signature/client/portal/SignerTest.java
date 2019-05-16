package no.digipost.signature.client.portal;

import no.digipost.signature.api.xml.XMLEmail;
import no.digipost.signature.api.xml.XMLNotifications;
import no.digipost.signature.api.xml.XMLSms;
import no.digipost.signature.client.portal.Signature.Signer;
import org.junit.jupiter.api.Test;

import static co.unruly.matchers.Java8Matchers.where;
import static co.unruly.matchers.Java8Matchers.whereNot;
import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByEmailAddress;
import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByEmailAddressAndMobileNumber;
import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByMobileNumber;
import static no.digipost.signature.client.portal.SignerIdentifier.identifiedByPersonalIdentificationNumber;
import static org.hamcrest.MatcherAssert.assertThat;

public class SignerTest {

    @Test
    public void all_kinds_of_signers_can_be_identified_by_a_signer_identifier() {
        Signer pinSigner = new Signer("00000000000", null);
        Signer emailSigner = new Signer(null, new XMLNotifications(new XMLEmail("email@example.com"), null));
        Signer smsSigner = new Signer(null, new XMLNotifications(null, new XMLSms("11111111")));
        Signer emailAndSmsSigner = new Signer(null, new XMLNotifications(new XMLEmail("email@example.com"), new XMLSms("11111111")));

        assertThat(identifiedByPersonalIdentificationNumber("00000000000"), where(pinSigner::isSameAs));
        assertThat(identifiedByPersonalIdentificationNumber("11111111111"), whereNot(pinSigner::isSameAs));
        assertThat(identifiedByEmailAddress("test@example.com"), whereNot(pinSigner::isSameAs));

        assertThat(identifiedByEmailAddress("email@example.com"), where(emailSigner::isSameAs));
        assertThat(identifiedByEmailAddress("other@example.com"), whereNot(emailSigner::isSameAs));
        assertThat(identifiedByEmailAddressAndMobileNumber("email@example.com", "11111111"), whereNot(emailSigner::isSameAs));

        assertThat(identifiedByMobileNumber("11111111"), where(smsSigner::isSameAs));
        assertThat(identifiedByMobileNumber("22222222"), whereNot(smsSigner::isSameAs));
        assertThat(identifiedByEmailAddressAndMobileNumber("email@example.com", "11111111"), whereNot(smsSigner::isSameAs));

        assertThat(identifiedByEmailAddressAndMobileNumber("email@example.com", "11111111"), where(emailAndSmsSigner::isSameAs));
        assertThat(identifiedByEmailAddressAndMobileNumber("other@example.com", "11111111"), whereNot(emailAndSmsSigner::isSameAs));
        assertThat(identifiedByEmailAddressAndMobileNumber("email@example.com", "00000000"), whereNot(emailAndSmsSigner::isSameAs));
        assertThat(identifiedByEmailAddress("email@example.com"), whereNot(emailAndSmsSigner::isSameAs));
        assertThat(identifiedByMobileNumber("11111111"), whereNot(emailAndSmsSigner::isSameAs));
    }

}
