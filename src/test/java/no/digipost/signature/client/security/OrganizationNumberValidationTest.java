package no.digipost.signature.client.security;

import no.digipost.signature.client.TestCertificates;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import static no.digipost.signature.client.security.CertificateChainValidation.Result.TRUSTED;
import static no.digipost.signature.client.security.CertificateChainValidation.Result.UNTRUSTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

public class OrganizationNumberValidationTest {

    @Test
    void trusts_Posten_Norge_SEID1_enterprise_certificate() {
        X509Certificate cert = TestCertificates.getOrganizationCertificateKeyStore().getCertificate();
        assertThat(cert.getSubjectX500Principal() + "is trusted",
                cert, where(c -> new OrganizationNumberValidation("988015814").validate(new X509Certificate[]{c}), is(TRUSTED)));
    }

    @Test
    void unexpected_organization_number_is_untrusted() {
        X509Certificate cert = TestCertificates.getOrganizationCertificateKeyStore().getCertificate();
        assertThat(cert.getSubjectX500Principal() + "is trusted",
                cert, where(c -> new OrganizationNumberValidation("0000").validate(new X509Certificate[]{c}), is(UNTRUSTED)));
    }


}
