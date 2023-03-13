package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.api.xml.XMLAvailability;
import no.digipost.signature.api.xml.XMLEmail;
import no.digipost.signature.api.xml.XMLEnabled;
import no.digipost.signature.api.xml.XMLHref;
import no.digipost.signature.api.xml.XMLNotifications;
import no.digipost.signature.api.xml.XMLNotificationsUsingLookup;
import no.digipost.signature.api.xml.XMLPortalDocument;
import no.digipost.signature.api.xml.XMLPortalSignatureJobManifest;
import no.digipost.signature.api.xml.XMLPortalSigner;
import no.digipost.signature.api.xml.XMLSender;
import no.digipost.signature.api.xml.XMLSms;
import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.OnBehalfOf;
import no.digipost.signature.client.core.SignatureType;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.portal.Notifications;
import no.digipost.signature.client.portal.NotificationsUsingLookup;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.digipost.signature.client.core.exceptions.SignerNotSpecifiedException.SIGNER_NOT_SPECIFIED;

public class CreatePortalManifest extends ManifestCreator<PortalJob> {

    private final MaySpecifySender defaultSenderConfiguration;
    private final Clock clock;

    public CreatePortalManifest(MaySpecifySender defaultSenderConfiguration, Clock clock) {
        this.defaultSenderConfiguration = defaultSenderConfiguration;
        this.clock = clock;
    }

    @Override
    XMLPortalSignatureJobManifest buildXmlManifest(PortalJob job) {
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

        ZonedDateTime activationTime = job.getActivationTime().map(activation -> activation.atZone(clock.getZone())).orElse(null);

        return new XMLPortalSignatureJobManifest()
                .withSigners(xmlSigners)
                .withRequiredAuthentication(job.getRequiredAuthentication().map(AuthenticationLevel::getXmlEnumValue).orElse(null))
                .withSender(new XMLSender().withOrganizationNumber(job.resolveSenderWithFallbackTo(defaultSenderConfiguration).getOrganizationNumber()))
                .withTitle(job.getTitle())
                .withNonsensitiveTitle(job.getNonsensitiveTitle().orElse(null))
                .withDescription(job.getDescription().orElse(null))
                .withDocuments(job.getDocuments().stream()
                        .map(document -> new XMLPortalDocument()
                                    .withTitle(document.getTitle())
                                    .withHref(XMLHref.of(document.getFileName()))
                                    .withMediaType(document.getMediaType()))
                        .collect(toList()))
                .withAvailability(new XMLAvailability()
                        .withActivationTime(activationTime)
                        .withAvailableSeconds(job.getAvailable().map(Duration::getSeconds).orElse(null)))
                .withIdentifierInSignedDocuments(job.getIdentifierInSignedDocuments().map(IdentifierInSignedDocuments::getXmlEnumValue).orElse(null));
    }

    private XMLPortalSigner generateSigner(PortalSigner signer) {
        XMLPortalSigner xmlSigner = new XMLPortalSigner()
                .withOrder(signer.getOrder())
                .withSignatureType(signer.getSignatureType().map(SignatureType::getXmlEnumValue).orElse(null))
                .withOnBehalfOf(signer.getOnBehalfOf().map(OnBehalfOf::getXmlEnumValue).orElse(null));

        if (signer.isIdentifiedByPersonalIdentificationNumber()) {
            xmlSigner.setPersonalIdentificationNumber(signer.getIdentifier().orElseThrow(SIGNER_NOT_SPECIFIED));
        } else {
            xmlSigner.setIdentifiedByContactInformation(new XMLEnabled());
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
