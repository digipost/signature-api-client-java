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
package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.client.core.Document;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.Signer;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.fail;

public class CreateManifestTest {

    @Test
    public void accept_valid_manifest() {
        CreateManifest createManifest = new CreateManifest();

        Document document = Document.builder("Subject", "Message", "file.txt", "hello".getBytes()).build();
        try {
            createManifest.createManifest(document, Collections.singletonList(new Signer("12345678910")), new Sender("123456789"));
        } catch (Exception e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

}
