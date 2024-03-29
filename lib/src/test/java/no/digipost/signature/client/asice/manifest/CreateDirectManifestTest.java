package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;
import no.digipost.signature.client.direct.ExitUrls;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static no.digipost.signature.client.core.DocumentType.TXT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class CreateDirectManifestTest {

    @Test
    void accept_valid_manifest() {
        MaySpecifySender defaultSenderConfig = () -> Optional.of(new Sender("123456789"));
        CreateDirectManifest createManifest = new CreateDirectManifest(defaultSenderConfig);

        DirectDocument document = DirectDocument.builder("Title", "hello".getBytes()).type(TXT).build();

        DirectJob job = DirectJob.builder(
                    "Job title",
                    document,
                    DirectSigner.withPersonalIdentificationNumber("12345678910").build(),
                    ExitUrls.of(URI.create("http://localhost/signed"), URI.create("http://localhost/canceled"), URI.create("http://localhost/failed")))
                .withIdentifierInSignedDocuments(IdentifierInSignedDocuments.NAME)
                .build();
        assertThat(createManifest, where(__ -> __.createManifest(job), instanceOf(Manifest.class)));
    }

}
