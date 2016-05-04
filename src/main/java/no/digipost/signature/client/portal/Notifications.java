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

    // The email notifications can't be turned off
    private boolean emailNotification = true;
    private String emailAddress = null;

    private boolean mobileNotification = false;
    private String mobileNumber = null;

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public boolean shouldSendEmailNotification() {
        return emailNotification;
    }

    public boolean shouldSendMobileNotification() {
        return mobileNotification;
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

        public Builder withOverriddenEmailAddress(String emailAddress) {
            target.emailNotification = true;
            target.emailAddress = emailAddress;
            return this;
        }

        public Builder withMobileNotification(boolean shouldSend) {
            target.mobileNotification = shouldSend;
            return this;
        }

        public Builder withOverriddenMobileNumber(String mobileNumber) {
            target.mobileNotification = true;
            target.mobileNumber = mobileNumber;
            return this;
        }

        public Notifications build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }
}
