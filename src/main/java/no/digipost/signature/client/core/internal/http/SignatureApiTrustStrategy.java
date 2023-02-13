package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.core.exceptions.SecurityException;
import no.digipost.signature.client.security.CertificateChainValidation;
import no.digipost.signature.client.security.CertificateChainValidation.Result;
import org.apache.hc.core5.ssl.TrustStrategy;

import java.security.cert.X509Certificate;

public final class SignatureApiTrustStrategy implements TrustStrategy {

    private final CertificateChainValidation certificateChainValidation;

    public SignatureApiTrustStrategy(CertificateChainValidation certificateChainValidation) {
        this.certificateChainValidation = certificateChainValidation;
    }

    /**
     * Verify that the server certificate is trusted.
     *
     * Note that we have to throw an Exception to make sure that invalid certificates will be denied.
     * The http client TrustStrategy can only be used to used to state that a server certificate is to be
     * trusted without consulting the standard Java certificate verification process.
     *
     * Unintuitively returns {@code false} when the {@link CertificateChainValidation} determines the chain
     * to be {@link Result#TRUSTED} to make sure http client will run the Java certificate verification process, which
     * will verify the certificate against the trust store, making sure that it's actually issued by a trusted CA.
     *
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String)
     */
    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) {
        Result result = certificateChainValidation.validate(chain);
        switch (result) {
            case TRUSTED_AND_SKIP_FURTHER_VALIDATION: return true;
            case TRUSTED: return false;
            case UNTRUSTED: default:
                String subjectDN = chain[0].getSubjectDN().getName();
                throw new SecurityException(
                    "Untrusted server certificate, according to " + certificateChainValidation + ". " +
                    "Make sure the server URI is correct. Actual certificate: " + subjectDN + ". " +
                    "This could indicate a misconfiguration of the client or server, or potentially a man-in-the-middle attack.");
        }
    }

}
