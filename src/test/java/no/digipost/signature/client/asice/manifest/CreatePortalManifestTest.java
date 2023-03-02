package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.portal.NotificationsUsingLookup;
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;

import static no.digipost.signature.client.core.DocumentType.TXT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class CreatePortalManifestTest {

    private final Clock clock = Clock.systemDefaultZone();

    @Test
    void accept_valid_manifest() {
        CreatePortalManifest createManifest = new CreatePortalManifest(MaySpecifySender.specifiedAs(new Sender("123456789")), clock);

        PortalDocument document = PortalDocument.builder("Title", "hello".getBytes()).type(TXT).build();

        PortalJob job = PortalJob.builder("Job title", document, PortalSigner.identifiedByPersonalIdentificationNumber("12345678910", NotificationsUsingLookup.EMAIL_ONLY).build())
                .withActivationTime(clock.instant())
                .availableFor(Duration.ofDays(30))
                .withDescription("Message")
                .withIdentifierInSignedDocuments(IdentifierInSignedDocuments.PERSONAL_IDENTIFICATION_NUMBER_AND_NAME)
                .build();
        assertThat(createManifest, where(__ -> __.createManifest(job), instanceOf(Manifest.class)));
    }

}
