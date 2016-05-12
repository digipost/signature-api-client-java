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

public class Notifications {

    private String emailAddress = null;
    private String mobileNumber = null;

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public boolean shouldSendEmail() {
        return emailAddress != null;
    }

    public boolean shouldSendSms() {
        return mobileNumber != null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Notifications target;
        private boolean built = false;

        private Builder() {
            target = new Notifications();
        }

        public Builder withEmailTo(String emailAddress) {
            target.emailAddress = emailAddress;
            return this;
        }

        public Builder withSmsTo(String mobileNumber) {
            target.mobileNumber = mobileNumber;
            return this;
        }

        public Notifications build() {
            if (built) throw new IllegalStateException("Can't build twice");
            if (target.emailAddress == null && target.mobileNumber == null) {
                throw new IllegalStateException("At least one way of notifying the signer must be specified");
            }
            built = true;
            return target;
        }
    }
}
