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
