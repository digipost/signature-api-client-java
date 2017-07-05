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
package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.api.xml.XMLAuthenticationLevel;
import no.digipost.signature.api.xml.XMLAvailability;
import no.digipost.signature.api.xml.XMLEmail;
import no.digipost.signature.api.xml.XMLEnabled;
import no.digipost.signature.api.xml.XMLLinkNotification;
import no.digipost.signature.api.xml.XMLNotifications;
import no.digipost.signature.api.xml.XMLNotificationsUsingLookup;
import no.digipost.signature.api.xml.XMLPortalDocument;
import no.digipost.signature.api.xml.XMLPortalSignatureJobManifest;
import no.digipost.signature.api.xml.XMLPortalSigner;
import no.digipost.signature.api.xml.XMLSender;
import no.digipost.signature.api.xml.XMLSignatureType;
import no.digipost.signature.api.xml.XMLSigningOnBehalfOf;
import no.digipost.signature.api.xml.XMLSms;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.internal.MarshallableEnum;
import no.digipost.signature.client.portal.Notifications;
import no.digipost.signature.client.portal.NotificationsUsingLookup;
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;

import java.util.ArrayList;
import java.util.List;

import static no.digipost.signature.client.core.internal.IdentifierType.EMAIL;
import static no.digipost.signature.client.core.internal.IdentifierType.MOBILE_NUMBER;

public class CreatePortalManifest extends ManifestCreator<PortalJob> {

    @Override
    Object buildXmlManifest(PortalJob job, Sender sender) {
        List<XMLPortalSigner> xmlSigners = new ArrayList<>();
        for (PortalSigner signer : job.getSigners()) {
            XMLPortalSigner xmlPortalSigner = generateSigner(signer);

            if (signer.getNotifications() != null) {
                xmlPortalSigner.setNotifications(generateNotifications(signer.getNotifications()));
            } else if (signer.getNotificationsUsingLookup() != null) {
                xmlPortalSigner.setNotificationsUsingLookup(generateNotificationsUsingLookup(signer.getNotificationsUsingLookup()));
            }
            xmlSigners.add(xmlPortalSigner);
        }

        PortalDocument document = job.getDocument();
        return new XMLPortalSignatureJobManifest()
                .withSigners(xmlSigners)
                .withRequiredAuthentication(job.getRequiredAuthentication().map(MarshallableEnum.To.<XMLAuthenticationLevel>xmlValue()).orNull())
                .withSender(new XMLSender().withOrganizationNumber(sender.getOrganizationNumber()))
                .withDocument(new XMLPortalDocument()
                        .withTitle(document.getTitle())
                        .withNonsensitiveTitle(document.getNonsensitiveTitle())
                        .withDescription(document.getMessage())
                        .withHref(document.getFileName())
                        .withMime(document.getMimeType())
                )
                .withAvailability(new XMLAvailability()
                        .withActivationTime(job.getActivationTime())
                        .withAvailableSeconds(job.getAvailableSeconds())
                );
    }

    private XMLPortalSigner generateSigner(PortalSigner signer) {
        XMLPortalSigner xmlSigner = new XMLPortalSigner()
                .withOrder(signer.getOrder())
                .withSignatureType(signer.getSignatureType().map(MarshallableEnum.To.<XMLSignatureType>xmlValue()).orNull())
                .withOnBehalfOf(signer.getOnBehalfOf().map(MarshallableEnum.To.<XMLSigningOnBehalfOf>xmlValue()).orNull());

        if (signer.isIdentifiedByPersonalIdentificationNumber()) {
            xmlSigner.setPersonalIdentificationNumber(signer.getIdentifier());
        } else {
            XMLEmail email = signer.getIdentifierType() == EMAIL ? new XMLEmail(signer.getIdentifier()) : null;
            XMLSms sms = signer.getIdentifierType() == MOBILE_NUMBER ? new XMLSms(signer.getIdentifier()) : null;
            xmlSigner.setLinkNotification(new XMLLinkNotification(sms, email));
        }
        return xmlSigner;
    }

    private XMLNotificationsUsingLookup generateNotificationsUsingLookup(NotificationsUsingLookup notificationsUsingLookup) {
        XMLNotificationsUsingLookup xmlNotificationsUsingLookup = new XMLNotificationsUsingLookup();
        if (notificationsUsingLookup.shouldSendEmail) {
            xmlNotificationsUsingLookup.setEmail(new XMLEnabled());
        }
        if (notificationsUsingLookup.shouldSendSms) {
            xmlNotificationsUsingLookup.setSms(new XMLEnabled());
        }
        return xmlNotificationsUsingLookup;
    }

    private XMLNotifications generateNotifications(Notifications notifications) {
        XMLNotifications xmlNotifications = new XMLNotifications();
        if (notifications.shouldSendEmail()) {
            xmlNotifications.setEmail(new XMLEmail().withAddress(notifications.getEmailAddress()));
        }
        if (notifications.shouldSendSms()) {
            xmlNotifications.setSms(new XMLSms().withNumber(notifications.getMobileNumber()));
        }
        return xmlNotifications;
    }
}
