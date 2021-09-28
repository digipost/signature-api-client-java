package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.TestCertificates;
import no.digipost.signature.client.core.exceptions.SecurityException;
import no.digipost.signature.client.security.CertificateChainValidation.Result;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import static no.digipost.signature.client.security.CertificateChainValidation.Result.TRUSTED;
import static no.digipost.signature.client.security.CertificateChainValidation.Result.TRUSTED_AND_SKIP_FURTHER_VALIDATION;
import static no.digipost.signature.client.security.CertificateChainValidation.Result.UNTRUSTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class SignatureApiTrustStrategyTest {

    private static final X509Certificate[] certChain = new X509Certificate[]{TestCertificates.getOrganizationCertificateKeyStore().getCertificate()};

    @Test
    void translates_TRUSTED_to_false_to_not_override_SSLContext_validation() {
        assertThat(TRUSTED, where(SignatureApiTrustStrategyTest::httpClientTrustStrategyTrust, is(false)));
    }

    @Test
    void translates_TRUSTED_AND_SKIP_FURTHER_VALIDATION_to_true() {
        assertThat(TRUSTED_AND_SKIP_FURTHER_VALIDATION, where(SignatureApiTrustStrategyTest::httpClientTrustStrategyTrust, is(true)));
    }

    @Test
    void translates_UNTRUSTED_to_throwing_exception() {
        assertThrows(SecurityException.class, () -> httpClientTrustStrategyTrust(UNTRUSTED));
    }

    private static boolean httpClientTrustStrategyTrust(Result result) {
        return new SignatureApiTrustStrategy(certChain -> result).isTrusted(certChain, "authType");
    }

}
