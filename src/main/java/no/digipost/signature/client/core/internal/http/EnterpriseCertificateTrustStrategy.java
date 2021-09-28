package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.core.exceptions.SecurityException;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class EnterpriseCertificateTrustStrategy implements TrustStrategy {

    /**
     * Used by some obscure cases to embed Norwegian "organisasjonsnummer" in certificates.
     */
    private static final String COMMON_NAME = "CN=";

    /**
     * Most common way to embed Norwegian "organisasjonsnummer" in certificates.
     */
    private static final String SERIALNUMBER = "SERIALNUMBER=";


    private final String trustedOrganizationNumber;
    private final List<String> acceptedSubstrings;

    public EnterpriseCertificateTrustStrategy(String trustedOrganizationNumber) {
        this.trustedOrganizationNumber = trustedOrganizationNumber;
        this.acceptedSubstrings = Stream.of(COMMON_NAME + trustedOrganizationNumber, SERIALNUMBER + trustedOrganizationNumber)
                .map(String::toLowerCase).collect(toList());
    }

    /**
     * Verify that the server certificate is issued to a trusted organization.
     *
     * Note that we have to throw an Exception to make sure that invalid certificates will be denied.
     * The http client TrustStrategy can only be used to used to state that a server certificate is to be
     * trusted without consulting the standard Java certificate verification process.
     *
     * Always returns false to make sure http client will run the Java certificate verification process, which
     * will verify the certificate against the trust store, making sure that it's actually issued by a trusted CA.
     *
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String)
     */
    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) {
        String subjectDN = chain[0].getSubjectDN().getName();

        if (!isTrustedEnterpriseCertiticate(subjectDN)) {
            throw new SecurityException(
                    "Could not find expected organization number '" + trustedOrganizationNumber + "' in server certificate. " +
                    "Make sure the server URI is correct. Actual certificate: " + subjectDN + ". " +
                    "This could indicate a misconfiguration of the client or server, or potentially a man-in-the-middle attack.");
        }

        return false;
    }

    private boolean isTrustedEnterpriseCertiticate(String subjectDN) {
        String lowerCaseSubjectDN = subjectDN.toLowerCase();
        for (String acceptedSubstring : this.acceptedSubstrings) {
            if (lowerCaseSubjectDN.contains(acceptedSubstring)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " trusting '" + trustedOrganizationNumber + "'";
    }

}
