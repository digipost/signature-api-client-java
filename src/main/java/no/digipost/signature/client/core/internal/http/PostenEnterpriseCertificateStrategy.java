package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.core.exceptions.SecurityException;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class PostenEnterpriseCertificateStrategy implements TrustStrategy {

    private static final String POSTEN_ORGANIZATION_NUMBER = "984661185";

    /**
     * Used by some obscure cases to embed Norwegian "organisasjonsnummer" in certificates.
     */
    private static final String COMMON_NAME_POSTEN = "CN=" + POSTEN_ORGANIZATION_NUMBER;

    /**
     * Most common way to embed Norwegian "organisasjonsnummer" in certificates.
     */
    private static final String SERIALNUMBER_POSTEN = "SERIALNUMBER=" + POSTEN_ORGANIZATION_NUMBER;


    /**
     * Verify that the server certificate is issued to Posten Norge AS.
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
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        String subjectDN = chain[0].getSubjectDN().getName();

        if (!isPostenEnterpriseCertiticate(subjectDN)) {
            throw new SecurityException("Could not find correct organization number in server certificate. Make sure the server URI is correct.\n" +
                    "Actual certificate: " + subjectDN + ".\n" +
                    "Expected certificate issued to organization number " + POSTEN_ORGANIZATION_NUMBER + "\n" +
                    "This could indicate a misconfiguration of the client or server, or potentially a man-in-the-middle attack.");
        }

        return false;
    }

    private boolean isPostenEnterpriseCertiticate(String subjectDN) {
        String lowerCaseSubjectDN = subjectDN.toLowerCase();
        return lowerCaseSubjectDN.contains(SERIALNUMBER_POSTEN.toLowerCase()) ||
                lowerCaseSubjectDN.contains(COMMON_NAME_POSTEN.toLowerCase());
    }

}
