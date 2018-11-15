package no.digipost.signature.client.core.internal;

import static java.util.Arrays.fill;

public final class PersonalIdentificationNumbers {

    public static String mask(String personalIdentificationNumber) {
        if (personalIdentificationNumber == null) {
            return null;
        } else if (personalIdentificationNumber.length() < 6) {
            return personalIdentificationNumber;
        }
        char[] masking = new char[personalIdentificationNumber.length() - 6];
        fill(masking, '*');
        return personalIdentificationNumber.substring(0, 6) + new String(masking);
    }

    private PersonalIdentificationNumbers() { }

}
