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

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.internal.KeyStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static java.util.Arrays.asList;

public class ClientConfiguration {

    private KeyStoreConfig keyStoreConfig;
    private Sender sender;
    private URI signatureServiceRoot = ServiceUri.PRODUCTION.uri;
    private List<String> certificateFolderPaths = Certificates.PRODUCTION.certificateFolderPaths;

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);

    private ClientConfiguration(KeyStoreConfig keyStoreConfig, Sender sender) {
        this.keyStoreConfig = keyStoreConfig;
        this.sender = sender;
    }

    public URI getSignatureServiceRoot() {
        return signatureServiceRoot;
    }

    public KeyStoreConfig getKeyStoreConfig() {
        return keyStoreConfig;
    }

    public Sender getSender() {
        return sender;
    }

    public List<String> getCertificateFolderPaths() {
        return certificateFolderPaths;
    }

    public static Builder builder(KeyStoreConfig keystore, Sender sender) {
        return new Builder(keystore, sender);
    }

    public static class Builder {

        private final ClientConfiguration target;

        private Builder(KeyStoreConfig keyStoreConfig, Sender sender) {
            this.target = new ClientConfiguration(keyStoreConfig, sender);
        }

        public Builder trustStore(Certificates certificates) {
            if (certificates == Certificates.TEST_AND_PRODUCTION) {
                log.warn("Using test certificates in trust store. This should never be done for production environments.");
            }

            this.target.certificateFolderPaths = certificates.certificateFolderPaths;
            return this;
        }

        /**
         * Set the service URI to one of the predefined environments.
         */
        public Builder serviceUri(ServiceUri environment) {
            this.target.signatureServiceRoot = environment.uri;
            return this;
        }

        /**
         * Override the service endpoint URI to a custom environment.
         */
        public Builder serviceUri(URI uri) {
            this.target.signatureServiceRoot = uri;
            return this;
        }

        /**
         * Override the trust store configuration to load DER-encoded certificates from the given folder(s).
         *
         * @see java.security.cert.CertificateFactory#generateCertificate(InputStream)
         */
        public Builder trustStore(String... certificatePath) {
            this.target.certificateFolderPaths = asList(certificatePath);
            return this;
        }

        public ClientConfiguration build() {
            return target;
        }

    }


    public enum Certificates {
        TEST_AND_PRODUCTION("classpath:/no/digipost/signature/client/certificates/prod", "classpath:/no/digipost/signature/client/certificates/test"),
        PRODUCTION("classpath:/no/digipost/signature/client/certificates/prod");

        private final List<String> certificateFolderPaths;

        Certificates(String... certificateFolderPaths) {
            this.certificateFolderPaths = asList(certificateFolderPaths);
        }
    }

    public enum ServiceUri {
        PRODUCTION(URI.create("https://api.signering.posten.no/api")),
        DIFI_QA(URI.create("https://api.difiqa.signering.posten.no/api")),
        DIFI_TEST(URI.create("https://api.difitest.signering.posten.no/api"));

        private final URI uri;

        ServiceUri(URI uri) {
            this.uri = uri;
        }
    }

}
