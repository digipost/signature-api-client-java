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
package no.digipost.signature.client;

import no.digipost.signature.client.internal.KeyStoreConfig;

import java.net.URI;

public class ClientConfiguration {

    private URI signatureServiceRoot;
    private KeyStoreConfig keyStoreConfig;

    public ClientConfiguration(URI signatureServiceRoot, KeyStoreConfig keyStoreConfig) {
        this.signatureServiceRoot = signatureServiceRoot;
        this.keyStoreConfig = keyStoreConfig;
    }

    public URI getSignatureServiceRoot() {
        return signatureServiceRoot;
    }

    public KeyStoreConfig getKeyStoreConfig() {
        return keyStoreConfig;
    }
}
