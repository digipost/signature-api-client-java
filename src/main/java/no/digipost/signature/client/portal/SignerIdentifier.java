/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
