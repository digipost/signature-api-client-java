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

import no.digipost.signature.client.core.SignatureType;
import no.digipost.signature.client.core.internal.SignerCustomizations;
import no.motif.Singular;
import no.motif.single.Optional;

import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;
import static no.motif.Singular.optional;

public class PortalSigner {

    private final String personalIdentificationNumber;
    private final Notifications notifications;
    private final NotificationsUsingLookup notificationsUsingLookup;
    private int order = 0;
    private Optional<SignatureType> signatureType = Singular.none();

    private PortalSigner(String personalIdentificationNumber, Notifications notifications, NotificationsUsingLookup notificationsUsingLookup) {
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.notifications = notifications;
        this.notificationsUsingLookup = notificationsUsingLookup;
    }

    public String getPersonalIdentificationNumber() {
        return personalIdentificationNumber;
    }

    public int getOrder() {
        return order;
    }

    public Notifications getNotifications() {
        return notifications;
    }

    public NotificationsUsingLookup getNotificationsUsingLookup() {
        return notificationsUsingLookup;
    }

    public Optional<SignatureType> getSignatureType() {
        return signatureType;
    }

    @Override
    public String toString() {
        return mask(personalIdentificationNumber);
    }

    public static Builder builder(String personalIdentificationNumber, Notifications notifications) {
        return new Builder(personalIdentificationNumber, notifications, null);
    }

    public static Builder builder(String personalIdentificationNumber, NotificationsUsingLookup notificationsUsingLookup) {
        return new Builder(personalIdentificationNumber, null, notificationsUsingLookup);
    }

    public static class Builder implements SignerCustomizations<Builder>{

        private final PortalSigner target;
        private boolean built = false;

        private Builder(String personalIdentificationNumber, Notifications notifications, NotificationsUsingLookup notificationsUsingLookup) {
            target = new PortalSigner(personalIdentificationNumber, notifications, notificationsUsingLookup);
        }

        public Builder withOrder(int order) {
            target.order = order;
            return this;
        }

        @Override
        public Builder withSignatureType(SignatureType type) {
            target.signatureType = optional(type);
            return this;
        }

        public PortalSigner build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }

    }
}
