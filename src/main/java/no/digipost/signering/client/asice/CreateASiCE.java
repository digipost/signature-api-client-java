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
package no.digipost.signering.client.asice;

import no.digipost.signering.client.asice.archive.Archive;
import no.digipost.signering.client.asice.archive.CreateZip;
import no.digipost.signering.client.asice.manifest.CreateManifest;
import no.digipost.signering.client.asice.manifest.Manifest;
import no.digipost.signering.client.asice.signature.CreateSignature;
import no.digipost.signering.client.asice.signature.Signature;
import no.digipost.signering.client.domain.Signeringsoppdrag;
import no.digipost.signering.client.internal.KeyStoreConfig;

import java.util.ArrayList;
import java.util.List;

public class CreateASiCE {

    private CreateZip createZip;
    private CreateManifest createManifest;
    private CreateSignature createSignature;

    public CreateASiCE() {
        createZip = new CreateZip();
        createManifest = new CreateManifest();
        createSignature = new CreateSignature();
    }

    public DocumentBundle createASiCE(final Signeringsoppdrag signeringsoppdrag, final KeyStoreConfig keyStoreConfig) {
        Manifest manifest = createManifest.createManifest(signeringsoppdrag);

        List<ASiCEAttachable> files = new ArrayList<>();
        files.add(signeringsoppdrag.getDokument());
        files.add(manifest);

        Signature signature = createSignature.createSignature(files, keyStoreConfig);
        files.add(signature);

        Archive archive = createZip.zipIt(files);

        return new DocumentBundle(archive.getBytes());
    }

}
