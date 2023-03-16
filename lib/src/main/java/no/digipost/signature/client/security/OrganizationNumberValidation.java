package no.digipost.signature.client.security;

import no.digipost.security.X509;

import java.security.cert.X509Certificate;

/**
 * Validates that the first certificate in a given certificate chain
 * is issued to a specific Norwegian enterprise by inspecting its
 * organization number.
 */
public class OrganizationNumberValidation implements CertificateChainValidation {

    private final String trustedOrganizationNumber;

    public OrganizationNumberValidation(String trustedOrganizationNumber) {
        this.trustedOrganizationNumber = trustedOrganizationNumber;
    }

    @Override
    public Result validate(X509Certificate[] certChain) {
        return X509
                .findOrganisasjonsnummer(certChain[0])
                .filter(trustedOrganizationNumber::equals)
                .map(trusted -> Result.TRUSTED)
                .orElse(Result.UNTRUSTED);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " trusting '" + trustedOrganizationNumber + "'";
    }

}
