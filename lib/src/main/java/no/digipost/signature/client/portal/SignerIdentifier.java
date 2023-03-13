package no.digipost.signature.client.portal;

public class SignerIdentifier {

    final String personalIdentificationNumber;
    final String emailAddress;
    final String mobileNumber;

    private SignerIdentifier(String personalIdentificationNumber, String emailAddress, String mobileNumber) {
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.emailAddress = emailAddress;
        this.mobileNumber = mobileNumber;
    }

    public static SignerIdentifier identifiedByPersonalIdentificationNumber(String personalIdentificationNumber) {
        return new SignerIdentifier(personalIdentificationNumber, null, null);
    }

    public static SignerIdentifier identifiedByEmailAddress(String emailAddress) {
        return new SignerIdentifier(null, emailAddress, null);
    }

    public static SignerIdentifier identifiedByMobileNumber(String mobileNumber) {
        return new SignerIdentifier(null, null, mobileNumber);
    }

    public static SignerIdentifier identifiedByEmailAddressAndMobileNumber(String emailAddress, String mobileNumber) {
        return new SignerIdentifier(null, emailAddress, mobileNumber);
    }

}
