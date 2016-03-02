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
import no.digipost.signature.client.TestKonfigurasjon;
import no.digipost.signature.client.asice.manifest.CreateDirectManifest;
import no.digipost.signature.client.asice.manifest.CreatePortalManifest;
import no.digipost.signature.client.asice.manifest.ManifestCreator;
import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.Signer;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.portal.PortalJob;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class CreateASiCETest {

    public static final Document DOCUMENT = Document.builder("Subject", "file.txt", "hello".getBytes())
            .message("Message")
            .fileType(Document.FileType.TXT)
            .build();

    public static final ClientConfiguration CLIENT_CONFIGURATION = ClientConfiguration.builder(TestKonfigurasjon.CLIENT_KEYSTORE).sender(new Sender("123456789")).build();

    @Test
    @Ignore("Writes files to disk. Can be useful for debugging")
    public void create_direct_asice_and_write_to_disk() throws IOException {
        DirectJob job = DirectJob.builder(new Signer("12345678910"), DOCUMENT, "https://completion.org", "https://cancellation.org", "https://error.org").build();

        create_document_bundle_and_write_to_disk(new CreateDirectManifest(), job);
    }

    @Test
    @Ignore("Writes files to disk. Can be useful for debugging")
    public void create_portal_asice_and_write_to_disk() throws IOException {
        PortalJob job = PortalJob.builder(DOCUMENT, new Signer("12345678910"))
                .withActivationTime(new Date())
                .withExpirationTime(new Date())
                .build();

        create_document_bundle_and_write_to_disk(new CreatePortalManifest(), job);
    }

    private <JOB extends SignatureJob> void create_document_bundle_and_write_to_disk(ManifestCreator<JOB> manifestCreator, JOB job) throws IOException {
        CreateASiCE<JOB> aSiCECreator = new CreateASiCE<>(manifestCreator, CLIENT_CONFIGURATION);
        DocumentBundle aSiCE = aSiCECreator.createASiCE(job);

        File tempFile = File.createTempFile("asice", ".zip");
        IOUtils.copy(new ByteArrayInputStream(aSiCE.getBytes()), new FileOutputStream(tempFile));
        System.out.println("Wrote document bundle to " + tempFile.getAbsolutePath());
    }

}
