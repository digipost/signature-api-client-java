package no.digipost.signature.client.security;

import javax.net.ssl.SSLContext;

import java.security.cert.X509Certificate;

public interface CertificateChainValidation {

    enum Result {
        /**
         * Indicates that the certificate chain is trusted by this particular
         * validation, but is subject to further validation by the {@link SSLContext}'s
         * configured trust manager. This should be considered as the default result
         * from a successful validation.
         */
        TRUSTED,

        /**
         * The certificate is determined to be trusted, <em>and</em> validation by the
         * {@link SSLContext}'s trust manager should be skipped. This result is not appropriate
         * for any integration with Posten signering, as it will effectively skip validating
         * the certificate to be issued by the trusted CA hierarchy.
         */
        TRUSTED_AND_SKIP_FURTHER_VALIDATION,

        /**
         * The certificate chain has been determined to be not trusted.
         */
        UNTRUSTED
    }

    Result validate(X509Certificate[] certChain);

}
