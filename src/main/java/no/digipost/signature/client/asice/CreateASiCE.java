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
import no.digipost.signature.client.asice.archive.CreateZip;
import no.digipost.signature.client.asice.manifest.Manifest;
import no.digipost.signature.client.asice.manifest.ManifestCreator;
import no.digipost.signature.client.asice.signature.CreateSignature;
import no.digipost.signature.client.asice.signature.Signature;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.internal.security.KeyStoreConfig;
import no.motif.single.Optional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.digipost.signature.client.core.exceptions.SenderNotSpecifiedException.SENDER_NOT_SPECIFIED;

public class CreateASiCE<JOB extends SignatureJob> {

    private final CreateZip createZip = new CreateZip();
    private final CreateSignature createSignature = new CreateSignature();

    private final ManifestCreator<JOB> manifestCreator;
    private final Optional<Sender> globalSender;
    private final KeyStoreConfig keyStoreConfig;
    private final Iterable<DocumentBundleProcessor> documentBundleProcessors;

    public CreateASiCE(ManifestCreator<JOB> manifestCreator, ClientConfiguration clientConfiguration) {
        this.manifestCreator = manifestCreator;
        this.globalSender = clientConfiguration.getGlobalSender();
        this.keyStoreConfig = clientConfiguration.getKeyStoreConfig();
        this.documentBundleProcessors = clientConfiguration.getDocumentBundleProcessors();
    }

    public DocumentBundle createASiCE(JOB job) {
        Sender sender = job.getSender()
                .or(globalSender)
                .orElseThrow(SENDER_NOT_SPECIFIED);

        Manifest manifest = manifestCreator.createManifest(job, sender);

        List<ASiCEAttachable> files = new ArrayList<>();
        files.add(job.getDocument());
        files.add(manifest);

        Signature signature = createSignature.createSignature(files, keyStoreConfig);
        files.add(signature);

        byte[] zipped = createZip.zipIt(files);
        for (DocumentBundleProcessor processor : documentBundleProcessors) {
            try (ByteArrayInputStream zipStream = new ByteArrayInputStream(zipped)) {
                processor.process(job, zipStream);
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }

        return new DocumentBundle(zipped);
    }

}
