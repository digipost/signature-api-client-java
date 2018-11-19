package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;
import no.digipost.signature.client.direct.ExitUrls;
import org.junit.Test;

import static org.junit.Assert.fail;

public class CreateDirectManifestTest {

    @Test
    public void accept_valid_manifest() {
        CreateDirectManifest createManifest = new CreateDirectManifest();

        DirectDocument document = DirectDocument.builder("Title", "file.txt", "hello".getBytes())
                .message("Message")
                .fileType(Document.FileType.TXT)
                .build();

        DirectJob job = DirectJob.builder(document, ExitUrls.of("http://localhost/signed", "http://localhost/canceled", "http://localhost/failed"), DirectSigner.withPersonalIdentificationNumber("12345678910").build())
                .withIdentifierInSignedDocuments(IdentifierInSignedDocuments.NAME)
                .build();
        try {
            createManifest.createManifest(job, new Sender("123456789"));
        } catch (Exception e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

}
