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

import no.digipost.signature.client.asice.archive.Archive;
import no.digipost.signature.client.asice.archive.CreateZip;
import no.digipost.signature.client.asice.manifest.CreateManifest;
import no.digipost.signature.client.asice.manifest.Manifest;
import no.digipost.signature.client.asice.signature.CreateSignature;
import no.digipost.signature.client.asice.signature.Signature;
import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.internal.KeyStoreConfig;

import java.util.ArrayList;
import java.util.List;

public class CreateASiCE {

    private static final CreateZip createZip = new CreateZip();
    private static final CreateManifest createManifest = new CreateManifest();
    private static final CreateSignature createSignature = new CreateSignature();

    public static DocumentBundle createASiCE(final Document document, final KeyStoreConfig keyStoreConfig) {
        Manifest manifest = createManifest.createManifest(document);

        List<ASiCEAttachable> files = new ArrayList<>();
        files.add(document);
        files.add(manifest);

        Signature signature = createSignature.createSignature(files, keyStoreConfig);
        files.add(signature);

        Archive archive = createZip.zipIt(files);

        return new DocumentBundle(archive.getBytes());
    }

}
