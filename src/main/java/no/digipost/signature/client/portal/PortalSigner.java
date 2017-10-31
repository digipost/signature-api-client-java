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

import no.digipost.signature.client.core.OnBehalfOf;
import no.digipost.signature.client.core.SignatureType;
import no.digipost.signature.client.core.internal.IdentifierType;
import no.digipost.signature.client.core.internal.SignerCustomizations;

import java.util.Optional;

import static no.digipost.signature.client.core.OnBehalfOf.OTHER;
import static no.digipost.signature.client.core.internal.IdentifierType.EMAIL;
import static no.digipost.signature.client.core.internal.IdentifierType.EMAIL_AND_MOBILE_NUMBER;
import static no.digipost.signature.client.core.internal.IdentifierType.MOBILE_NUMBER;
import static no.digipost.signature.client.core.internal.IdentifierType.PERSONAL_IDENTIFICATION_NUMBER;
import static no.digipost.signature.client.core.internal.PersonalIdentificationNumbers.mask;

public class PortalSigner {

    private final IdentifierType identifierType;
    private final Optional<String> identifier;

    private Notifications notifications;
    private NotificationsUsingLookup notificationsUsingLookup;

    private int order = 0;
    private Optional<SignatureType> signatureType = Optional.empty();
    private Optional<OnBehalfOf> onBehalfOf = Optional.empty();

    private PortalSigner(IdentifierType identifierType, Notifications notifications) {
        this.identifier = Optional.empty();
        this.identifierType = identifierType;
        this.notifications = notifications;
    }

    private PortalSigner(String personalIdentificationNumber, Notifications notifications, NotificationsUsingLookup notificationsUsingLookup) {
        this.identifier = Optional.of(personalIdentificationNumber);
        this.identifierType = PERSONAL_IDENTIFICATION_NUMBER;
        this.notifications = notifications;
        this.notificationsUsingLookup = notificationsUsingLookup;
    }

    /**
     * @deprecated See {@link #identifiedByPersonalIdentificationNumber(String, Notifications)}
     */
    @Deprecated
    public static Builder builder(String personalIdentificationNumber, Notifications notifications) {
        return new Builder(personalIdentificationNumber, notifications, null);
    }

    /**
     * @deprecated See {@link #identifiedByPersonalIdentificationNumber(String, NotificationsUsingLookup)}
     */
    @Deprecated
    public static Builder builder(String personalIdentificationNumber, NotificationsUsingLookup notificationsUsingLookup) {
        return new Builder(personalIdentificationNumber, null, notificationsUsingLookup);
    }

    public static Builder identifiedByPersonalIdentificationNumber(String personalIdentificationNumber, Notifications notifications) {
        return new Builder(personalIdentificationNumber, notifications, null);
    }

    public static Builder identifiedByPersonalIdentificationNumber(String personalIdentificationNumber, NotificationsUsingLookup notificationsUsingLookup) {
        return new Builder(personalIdentificationNumber, null, notificationsUsingLookup);
    }

    public static Builder identifiedByEmail(String emailAddress) {
        return new Builder(EMAIL, Notifications.builder().withEmailTo(emailAddress).build());
    }

    public static Builder identifiedByMobileNumber(String number) {
        return new Builder(MOBILE_NUMBER, Notifications.builder().withSmsTo(number).build());
    }

    public static Builder identifiedByEmailAndMobileNumber(String emailAddress, String number) {
        return new Builder(EMAIL_AND_MOBILE_NUMBER, Notifications.builder().withEmailTo(emailAddress).withSmsTo(number).build());
    }

    public boolean isIdentifiedByPersonalIdentificationNumber() {
        return identifierType == PERSONAL_IDENTIFICATION_NUMBER;
    }

    public Optional<String> getIdentifier() {
        return identifier;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
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

    public Optional<OnBehalfOf> getOnBehalfOf() {
        return onBehalfOf;
    }

    @Override
    public String toString() {
        return isIdentifiedByPersonalIdentificationNumber() ? mask(identifier.get()) : "Signer with " + notifications;
    }


    public static class Builder implements SignerCustomizations<Builder> {

        private final PortalSigner target;
        private boolean built = false;

        private Builder(String personalIdentificationNumber, Notifications notifications, NotificationsUsingLookup notificationsUsingLookup) {
            target = new PortalSigner(personalIdentificationNumber, notifications, notificationsUsingLookup);
        }

        private Builder(IdentifierType identifierType, Notifications notifications) {
            target = new PortalSigner(identifierType, notifications);
        }

        public Builder withOrder(int order) {
            target.order = order;
            return this;
        }

        @Override
        public Builder withSignatureType(SignatureType type) {
            target.signatureType = Optional.of(type);
            return this;
        }

        @Override
        public Builder onBehalfOf(OnBehalfOf onBehalfOf) {
            target.onBehalfOf = Optional.of(onBehalfOf);
            return this;
        }

        public PortalSigner build() {
            if (target.onBehalfOf.isPresent() && target.onBehalfOf.get() == OTHER && target.notificationsUsingLookup != null) {
                throw new IllegalStateException("Can't look up contact information for notifications when signing on behalf of a third party");
            }
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }

    }
}
