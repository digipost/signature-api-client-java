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

public class NotificationsUsingLookup {

    // Email notifications can't be turned off
    private final boolean email = true;
    private boolean sms = false;

    public boolean shouldSendEmail() {
        return email;
    }

    public boolean shouldSendSms() {
        return sms;
    }

    public static Builder notifyByEMail() {
        return new Builder();
    }

    public static class Builder {

        private final NotificationsUsingLookup target;
        private boolean built = false;

        private Builder() {
            target = new NotificationsUsingLookup();
        }

        public Builder andSms() {
            target.sms = true;
            return this;
        }

        public NotificationsUsingLookup build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }

}
