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
package no.digipost.signature.client.direct;

public class DirectSigner {

    public static DirectSigner withPersonalIdentificationNumber(String personalIdentificationNumber) {
        return new DirectSigner(personalIdentificationNumber, null);
    }

    public static DirectSigner withCustomIdentifier(String customIdentifier) {
        return new DirectSigner(null, customIdentifier);
    }



    private final String personalIdentificationNumber;
    private final String customIdentifier;

    private DirectSigner(String personalIdentificationNumber, String customIdentifier) {
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.customIdentifier = customIdentifier;
    }

    public boolean isIdentifiedByPersonalIdentificationNumber() {
        return personalIdentificationNumber != null;
    }

    public String getPersonalIdentificationNumber() {
        if (!isIdentifiedByPersonalIdentificationNumber()) {
            throw new IllegalStateException(this + " is not identified by personal identification number, use getCustomIdentifier() instead.");
        }
        return personalIdentificationNumber;
    }

    public String getCustomIdentifier() {
        if (customIdentifier == null) {
            throw new IllegalStateException(this + " is not identified by a custom identifier, use getPersonalIdentificationNumber() instead.");
        }
        return customIdentifier;
    }

    @Override
    public String toString() {
        return DirectSigner.class.getSimpleName() + ": " + (isIdentifiedByPersonalIdentificationNumber() ? mask(personalIdentificationNumber) : customIdentifier);
    }

    static String mask(String personalIdentificationNumber) {
        return personalIdentificationNumber.substring(0, 6) + "*****";
    }

}
