package no.digipost.signature.client.security;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Validates that the first certificate in a given certificate chain
 * is issued to a specific Norwegian enterprise by inspecting its
 * organization number.
 */
public class OrganizationNumberValidation implements CertificateChainValidation {

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

    public OrganizationNumberValidation(String trustedOrganizationNumber) {
        this.trustedOrganizationNumber = trustedOrganizationNumber;
        this.acceptedSubstrings = Stream.of(COMMON_NAME + trustedOrganizationNumber, SERIALNUMBER + trustedOrganizationNumber)
                .map(String::toLowerCase).collect(toList());
    }

    @Override
    public Result validate(X509Certificate[] certChain) {
        String subjectDN = certChain[0].getSubjectDN().getName();
        String lowerCaseSubjectDN = subjectDN.toLowerCase();
        for (String acceptedSubstring : this.acceptedSubstrings) {
            if (lowerCaseSubjectDN.contains(acceptedSubstring)) {
                return Result.TRUSTED;
            }
        }
        return Result.UNTRUSTED;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " trusting '" + trustedOrganizationNumber + "'";
    }

}
