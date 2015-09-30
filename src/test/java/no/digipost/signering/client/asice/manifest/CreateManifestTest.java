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
package no.digipost.signering.client.asice.manifest;

import no.digipost.signering.client.domain.Dokument;
import no.digipost.signering.client.domain.Signeringsoppdrag;
import org.junit.Test;

import static org.junit.Assert.fail;

public class CreateManifestTest {

    @Test
    public void accept_valid_signeringsoppdrag() {
        CreateManifest createManifest = new CreateManifest();

        Dokument dokument = Dokument.builder("Emne", "fil.txt", "hei".getBytes()).build();
        Signeringsoppdrag signeringsoppdrag = Signeringsoppdrag.builder("01010100001", dokument, "http://localhost").build();

        try {
            createManifest.createManifest(signeringsoppdrag);
        } catch (Exception e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

}
