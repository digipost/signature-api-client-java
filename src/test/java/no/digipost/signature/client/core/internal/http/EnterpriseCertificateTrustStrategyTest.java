package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.TestCertificates;
import org.apache.http.conn.ssl.TrustStrategy;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import static org.hamcrest.MatcherAssert.assertThat;

class EnterpriseCertificateTrustStrategyTest {

    @Test
    void trustsSeid1PostenEnterpriseCert() {
        EnterpriseCertificateTrustStrategy trustStrategy = new EnterpriseCertificateTrustStrategy("988015814");
        X509Certificate cert = TestCertificates.getOrganizationCertificateKeyStore().getCertificate();
        assertThat(cert, isAllowedFurtherTrustManagerValidationBy(trustStrategy));
    }


    private static Matcher<X509Certificate> isAllowedFurtherTrustManagerValidationBy(TrustStrategy strategy) {
        return new TypeSafeDiagnosingMatcher<X509Certificate>() {
            @Override
            public void describeTo(Description description) {
                description
                    .appendText("is allowed to proceed to further TrustManager validation by ").appendValue(strategy);
            }

            @Override
            protected boolean matchesSafely(X509Certificate cert, Description mismatchDescription) {
                try {
                    boolean trustedAndMayIgnoreFurtherValidation = strategy.isTrusted(new X509Certificate[]{cert}, "key exchange algorithm - not relevant");
                    return !trustedAndMayIgnoreFurtherValidation;
                } catch (Exception e) {
                    mismatchDescription
                        .appendText("the certificate ").appendValue(cert.getSubjectX500Principal()).appendText(" was not trusted");
                    return false;
                }
            }
        };
    }

}
