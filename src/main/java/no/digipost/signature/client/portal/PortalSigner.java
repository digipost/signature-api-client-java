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

public class PortalSigner {

    private String personalIdentificationNumber;
    private int order;

    public PortalSigner(String personalIdentificationNumber) {
        this(personalIdentificationNumber, 0);
    }

    public PortalSigner(String personalIdentificationNumber, int order) {
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.order = order;
    }

    public String getPersonalIdentificationNumber() {
        return personalIdentificationNumber;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return mask(personalIdentificationNumber);
    }

    static String mask(String personalIdentificationNumber) {
        return personalIdentificationNumber.substring(0, 6) + "*****";
    }
}
