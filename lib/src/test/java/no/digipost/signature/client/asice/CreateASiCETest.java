package no.digipost.signature.client.asice;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.manifest.CreateDirectManifest;
import no.digipost.signature.client.asice.manifest.CreatePortalManifest;
import no.digipost.signature.client.asice.manifest.ManifestCreator;
import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.DocumentType;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;
import no.digipost.signature.client.portal.NotificationsUsingLookup;
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.Files.newDirectoryStream;
import static java.util.Arrays.asList;
import static java.util.stream.Stream.concat;
import static no.digipost.DiggExceptions.applyUnchecked;
import static no.digipost.DiggExceptions.getUnchecked;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static no.digipost.signature.client.asice.DumpDocumentBundleToDisk.TIMESTAMP_PATTERN;
import static no.digipost.signature.client.asice.DumpDocumentBundleToDisk.referenceFilenamePart;
import static no.digipost.signature.client.direct.ExitUrls.singleExitUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class CreateASiCETest {

    private static final Clock clock = Clock.systemDefaultZone();

    private static Path dumpFolder; static {
        Path testFileSystemLocation = getUnchecked(() -> Paths.get(CreateASiCETest.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        dumpFolder = testFileSystemLocation.getParent()
                .resolve(CreateASiCETest.class.getSimpleName())
                .resolve(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).format(ZonedDateTime.now(clock)));
        applyUnchecked(Files::createDirectories, dumpFolder);
    }

    private static final ClientConfiguration config = ClientConfiguration.builder(CLIENT_KEYSTORE)
            .defaultSender(new Sender("123456789"))
            .enableDocumentBundleDiskDump(dumpFolder)
            .build();

    private static final DirectDocument DIRECT_DOCUMENT = DirectDocument.builder("Document title", "hello".getBytes())
            .type(DocumentType.TXT)
            .build();

    private static final PortalDocument PORTAL_DOCUMENT = PortalDocument.builder("Document title", "hello".getBytes())
            .type(DocumentType.TXT)
            .build();


    @Test
    public void create_direct_asice_and_write_to_disk() throws IOException {
        DirectJob job = DirectJob.builder("Job title",
                    asList(DIRECT_DOCUMENT, DIRECT_DOCUMENT),
                    asList(DirectSigner.withPersonalIdentificationNumber("12345678910").build()),
                    singleExitUrl(URI.create("https://job.well.done.org")))
                .withReference("direct job")
                .build();

        create_document_bundle_and_dump_to_disk(new CreateDirectManifest(config.getDefaultSender()), job);
    }

    @Test
    public void create_portal_asice_and_write_to_disk() throws IOException {
        PortalJob job = PortalJob.builder("Job title",
                    asList(PORTAL_DOCUMENT, PORTAL_DOCUMENT, PORTAL_DOCUMENT),
                    asList(PortalSigner.identifiedByPersonalIdentificationNumber("12345678910", NotificationsUsingLookup.EMAIL_ONLY).build()))
                .withReference("portal job")
                .withDescription("Message")
                .withActivationTime(clock.instant())
                .availableFor(Duration.ofDays(30))
                .build();

        create_document_bundle_and_dump_to_disk(new CreatePortalManifest(config.getDefaultSender(), clock), job);
    }

    private <JOB extends SignatureJob> void create_document_bundle_and_dump_to_disk(ManifestCreator<JOB> manifestCreator, JOB job) throws IOException {

        CreateASiCE<JOB> aSiCECreator = new CreateASiCE<>(manifestCreator, config);
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
        assertThat(fileNames, containsInAnyOrder(concat(
                    job.getDocuments().stream().map(Document::getFileName),
                    Stream.of("manifest.xml", "META-INF/signatures.xml"))
                .toArray(String[]::new)));
    }

}
