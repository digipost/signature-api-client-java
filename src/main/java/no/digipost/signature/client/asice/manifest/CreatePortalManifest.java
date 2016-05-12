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

import no.digipost.signature.api.xml.*;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.portal.*;

public class CreatePortalManifest extends ManifestCreator<PortalJob> {

    @Override
    Object buildXmlManifest(PortalJob job, Sender sender) {
        XMLPortalSigners xmlSigners = new XMLPortalSigners();
        for (PortalSigner signer : job.getSigners()) {
            XMLPortalSigner xmlPortalSigner = new XMLPortalSigner()
                    .withPersonalIdentificationNumber(signer.getPersonalIdentificationNumber())
                    .withOrder(signer.getOrder());

            if (signer.getNotifications() != null) {
                xmlPortalSigner.setNotifications(generateNotifications(signer.getNotifications()));
            } else if (signer.getNotificationsUsingLookup() != null) {
                xmlPortalSigner.setNotificationsUsingLookup(generateNotificationsUsingLookup(signer.getNotificationsUsingLookup()));
            }

            xmlSigners.getSigners().add(xmlPortalSigner);
        }

        PortalDocument document = job.getDocument();
        return new XMLPortalSignatureJobManifest()
                .withSigners(xmlSigners)
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

    private XMLNotificationsUsingLookup generateNotificationsUsingLookup(NotificationsUsingLookup notificationsUsingLookup) {
        XMLNotificationsUsingLookup xmlNotificationsUsingLookup = new XMLNotificationsUsingLookup();
        if (notificationsUsingLookup.shouldSendEmail()) {
            xmlNotificationsUsingLookup.setEmail(new XMLEnabled());
        }
        if (notificationsUsingLookup.shouldSendSms()) {
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
