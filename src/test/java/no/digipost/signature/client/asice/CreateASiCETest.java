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
import no.digipost.signature.client.portal.PortalDocument;
import no.digipost.signature.client.portal.PortalJob;
import no.digipost.signature.client.portal.PortalSigner;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.Files.newDirectoryStream;
import static java.util.concurrent.TimeUnit.DAYS;
import static no.digipost.signature.client.TestKonfigurasjon.CLIENT_KEYSTORE;
import static no.digipost.signature.client.asice.DumpDocumentBundleToDisk.referenceFilenamePart;
import static no.digipost.signature.client.direct.ExitUrls.singleExitUrl;
import static no.motif.Iterate.on;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class CreateASiCETest {

    @BeforeClass
    public static void initTempFolder() throws URISyntaxException, IOException {
        dumpFolder = Paths.get(CreateASiCETest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent()
                .resolve(CreateASiCETest.class.getSimpleName())
                .resolve(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
        Files.createDirectories(dumpFolder);
    }

    public static Path dumpFolder;

    public static final DirectDocument DIRECT_DOCUMENT = DirectDocument.builder("Subject", "file.txt", "hello".getBytes())
            .message("Message")
            .fileType(Document.FileType.TXT)
            .build();

    public static final PortalDocument PORTAL_DOCUMENT = PortalDocument.builder("Subject", "file.txt", "hello".getBytes())
            .message("Message")
            .fileType(Document.FileType.TXT)
            .build();



    @Test
    public void create_direct_asice_and_write_to_disk() throws IOException {
        DirectJob job = DirectJob.builder(DirectSigner.builder("12345678910").build(), DIRECT_DOCUMENT, singleExitUrl("https://job.well.done.org"))
                .withReference("direct job")
                .build();

        create_document_bundle_and_dump_to_disk(new CreateDirectManifest(), job);
    }

    @Test
    public void create_portal_asice_and_write_to_disk() throws IOException {
        PortalJob job = PortalJob.builder(PORTAL_DOCUMENT, PortalSigner.builder("12345678910").build())
                .withReference("portal job")
                .withActivationTime(new Date())
                .availableFor(30, DAYS)
                .build();

        create_document_bundle_and_dump_to_disk(new CreatePortalManifest(), job);
    }

    private <JOB extends SignatureJob> void create_document_bundle_and_dump_to_disk(ManifestCreator<JOB> manifestCreator, JOB job) throws IOException {
        CreateASiCE<JOB> aSiCECreator = new CreateASiCE<>(manifestCreator, ClientConfiguration.builder(CLIENT_KEYSTORE)
                .globalSender(new Sender("123456789"))
                .enableDocumentBundleDiskDump(dumpFolder)
                .build());
        aSiCECreator.createASiCE(job);

        Path asiceFile;
        try (DirectoryStream<Path> dumpedFileStream = newDirectoryStream(dumpFolder, "*-" + referenceFilenamePart.$(job.getReference()) + "*.zip")) {
            asiceFile = on(dumpedFileStream).eval().head().get();
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
