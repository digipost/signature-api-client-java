package no.digipost.signature.client.asice;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.manifest.CreateDirectManifest;
import no.digipost.signature.client.asice.manifest.CreatePortalManifest;
import no.digipost.signature.client.asice.manifest.ManifestCreator;
import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;
import no.digipost.signature.client.portal.NotificationsUsingLookup;
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.Files.newDirectoryStream;
import static java.util.concurrent.TimeUnit.DAYS;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static no.digipost.signature.client.asice.DumpDocumentBundleToDisk.TIMESTAMP_PATTERN;
import static no.digipost.signature.client.asice.DumpDocumentBundleToDisk.referenceFilenamePart;
import static no.digipost.signature.client.direct.ExitUrls.singleExitUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class CreateASiCETest {

    private static final Clock clock = Clock.systemDefaultZone();

    @BeforeAll
    public static void initTempFolder() throws URISyntaxException, IOException {
        dumpFolder = Paths.get(CreateASiCETest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent()
                .resolve(CreateASiCETest.class.getSimpleName())
                .resolve(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).format(ZonedDateTime.now(clock)));
        Files.createDirectories(dumpFolder);
    }

    private static Path dumpFolder;

    private static final DirectDocument DIRECT_DOCUMENT = DirectDocument.builder("Document title", "file.txt", "hello".getBytes())
            .fileType(Document.FileType.TXT)
            .build();

    private static final PortalDocument PORTAL_DOCUMENT = PortalDocument.builder("Document title", "file.txt", "hello".getBytes())
            .fileType(Document.FileType.TXT)
            .build();


    @Test
    public void create_direct_asice_and_write_to_disk() throws IOException {
        DirectJob job = DirectJob.builder("Job title", DIRECT_DOCUMENT, singleExitUrl(URI.create("https://job.well.done.org")), DirectSigner.withPersonalIdentificationNumber("12345678910").build())
                .withReference("direct job")
                .build();

        create_document_bundle_and_dump_to_disk(new CreateDirectManifest(), job);
    }

    @Test
    public void create_portal_asice_and_write_to_disk() throws IOException {
        PortalJob job = PortalJob.builder("Job title", PORTAL_DOCUMENT, PortalSigner.identifiedByPersonalIdentificationNumber("12345678910", NotificationsUsingLookup.EMAIL_ONLY).build())
                .withReference("portal job")
                .withDescription("Message")
                .withActivationTime(clock.instant())
                .availableFor(30, DAYS)
                .build();

        create_document_bundle_and_dump_to_disk(new CreatePortalManifest(clock), job);
    }

    private <JOB extends SignatureJob> void create_document_bundle_and_dump_to_disk(ManifestCreator<JOB> manifestCreator, JOB job) throws IOException {
        CreateASiCE<JOB> aSiCECreator = new CreateASiCE<>(manifestCreator, ClientConfiguration.builder(CLIENT_KEYSTORE)
                .globalSender(new Sender("123456789"))
                .enableDocumentBundleDiskDump(dumpFolder)
                .build());
        aSiCECreator.createASiCE(job);

        Path asiceFile;
        try (DirectoryStream<Path> dumpedFileStream = newDirectoryStream(dumpFolder, "*-" + referenceFilenamePart.apply(job.getReference()) + "*.zip")) {
            asiceFile = dumpedFileStream.iterator().next();
        }

        List<String> fileNames = new ArrayList<>();
        try (InputStream asiceStream = Files.newInputStream(asiceFile); ZipInputStream uncompressed = new ZipInputStream(asiceStream)) {
            for (ZipEntry entry = uncompressed.getNextEntry(); entry != null; entry = uncompressed.getNextEntry()) {
                fileNames.add(entry.getName());
            }
        }
        assertThat(fileNames, hasItem(job.getDocument().getFileName()));
        assertThat(fileNames, hasItem("manifest.xml"));
        assertThat(fileNames, hasItem("META-INF/signatures.xml"));
    }

}
