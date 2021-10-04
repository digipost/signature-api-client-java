package no.digipost.signature.client.security;

import no.digipost.signature.client.TestCertificates;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static no.digipost.signature.client.security.CertificateChainValidation.Result.TRUSTED;
import static no.digipost.signature.client.security.CertificateChainValidation.Result.UNTRUSTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

public class OrganizationNumberValidationTest {

    @Test
    void trusts_SEID1_enterprise_certificate() {
        X509Certificate seid1Cert = TestCertificates.getOrganizationCertificateKeyStore().getCertificate();
        assertThat(seid1Cert.getSubjectX500Principal() + "is trusted",
                seid1Cert, where(c -> new OrganizationNumberValidation("988015814").validate(new X509Certificate[]{c}), is(TRUSTED)));
    }

    @Test
    void trusts_SEID2_enterprise_certificate() throws CertificateException, IOException {
        X509Certificate seid2Cert;
        try (InputStream certContents = getClass().getResourceAsStream("/test4-autentiseringssertifikat-vid-europa.cer")) {
            seid2Cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certContents);
        }
        assertThat(seid2Cert.getSubjectX500Principal() + "is trusted",
                seid2Cert, where(c -> new OrganizationNumberValidation("100101688").validate(new X509Certificate[]{c}), is(TRUSTED)));
    }

    @Test
    void unexpected_organization_number_is_untrusted() {
        X509Certificate cert = TestCertificates.getOrganizationCertificateKeyStore().getCertificate();
        assertThat(cert.getSubjectX500Principal() + "is trusted",
                cert, where(c -> new OrganizationNumberValidation("0000").validate(new X509Certificate[]{c}), is(UNTRUSTED)));
    }


}
