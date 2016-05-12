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

    private String personalIdentificationNumber;

    private DirectSigner(String personalIdentificationNumber) {
        this.personalIdentificationNumber = personalIdentificationNumber;
    }

    public String getPersonalIdentificationNumber() {
        return personalIdentificationNumber;
    }

    @Override
    public String toString() {
        return mask(personalIdentificationNumber);
    }

    static String mask(String personalIdentificationNumber) {
        return personalIdentificationNumber.substring(0, 6) + "*****";
    }

    public static Builder builder(String personalIdentificationNumber) {
        return new Builder(personalIdentificationNumber);
    }

    public static class Builder {

        private final DirectSigner target;
        private boolean built = false;

        private Builder(String personalIdentificationNumber) {
            target = new DirectSigner(personalIdentificationNumber);
        }

        public DirectSigner build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }
}
