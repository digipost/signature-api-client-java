package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.core.exceptions.SecurityException;
import no.digipost.signature.client.security.CertificateChainValidation;
import no.digipost.signature.client.security.CertificateChainValidation.Result;
import org.apache.hc.core5.ssl.TrustStrategy;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static javax.security.auth.x500.X500Principal.RFC1779;

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
                String certificateDescription = Optional.ofNullable(chain)
                        .filter(certs -> certs.length > 0)
                        .map(certs -> certs[0])
                        .map(cert -> {
                            String subjectDN = cert.getSubjectX500Principal().getName(RFC1779);
                            BigInteger serialNumber = cert.getSerialNumber();
                            String issuerDN = cert.getIssuerX500Principal().getName(RFC1779);
                            ZonedDateTime expires = cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault());
                            return subjectDN + " (serial number " + serialNumber + ", expires " + expires + "), issued by " + issuerDN;
                        })
                        .orElse("<no server certificate>");
                throw new SecurityException(
                    "Untrusted server certificate, according to " + certificateChainValidation + ". " +
                    "Actual certificate from server response: " + certificateDescription + ". " +
                    "This normally indicates either a misconfiguration of this client library, or a mixup of URLs used to communicate with the API. " +
                    "Make sure the request URL is correct, is actually for the API, and it aligns with the configured ServiceEnvironment. " +
                    "It should e.g. not be a URL that is to be accessed by a user from a web browser.");
        }
    }

}
