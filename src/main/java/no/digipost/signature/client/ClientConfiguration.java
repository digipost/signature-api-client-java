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
import static no.digipost.signature.client.ClientConfiguration.Certificates.TEST;

public class ClientConfiguration {

    /**
     * Socket timeout is used for both requests and, if any,
     * underlying layered sockets (typically for
     * secure sockets). The default value is {@value #DEFAULT_SOCKET_TIMEOUT_MS} ms.
     */
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 10_000;

    /**
     * The default connect timeout for requests: {@value #DEFAULT_CONNECT_TIMEOUT_MS} ms.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000;

    private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
    private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
    private KeyStoreConfig keyStoreConfig;
    private Sender sender;
    private URI signatureServiceRoot = ServiceUri.PRODUCTION.uri;
    private List<String> certificatePaths = Certificates.PRODUCTION.certificatePaths;

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

    public List<String> getCertificatePaths() {
        return certificatePaths;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMs;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMs;
    }

    public static Builder builder(KeyStoreConfig keystore, Sender sender) {
        return new Builder(keystore, sender);
    }

    public static class Builder {

        private final ClientConfiguration target;

        private Builder(KeyStoreConfig keyStoreConfig, Sender sender) {
            this.target = new ClientConfiguration(keyStoreConfig, sender);
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
         * Override the
         * {@link ClientConfiguration#DEFAULT_SOCKET_TIMEOUT_MS default socket timeout value}.
         */
        public Builder socketTimeoutMillis(int millis) {
            this.target.socketTimeoutMs = millis;
            return this;
        }

        /**
         * Override the
         * {@link ClientConfiguration#DEFAULT_CONNECT_TIMEOUT_MS default connect timeout value}.
         */
        public Builder connectTimeoutMillis(int millis) {
            this.target.connectTimeoutMs = millis;
            return this;
        }

        public Builder trustStore(Certificates certificates) {
            if (certificates.equals(TEST)) {
                log.warn("Using test certificates in trust store. This should never be done for production environments.");
            }

            this.target.certificatePaths = certificates.certificatePaths;
            return this;
        }

        /**
         * Override the trust store configuration to load DER-encoded certificates from the given folder(s).
         *
         * @see java.security.cert.CertificateFactory#generateCertificate(InputStream)
         */
        public Builder trustStore(String... certificatePath) {
            this.target.certificatePaths = asList(certificatePath);
            return this;
        }

        public ClientConfiguration build() {
            return target;
        }

    }


    public enum Certificates {

        TEST(asList(
                "classpath:certificates/test/Buypass_Class_3_Test4_CA_3.cer",
                "classpath:certificates/test/Buypass_Class_3_Test4_Root_CA.cer",
                "classpath:certificates/test/commfides_test_ca.cer",
                "classpath:certificates/test/commfides_test_root_ca.cer",
                "classpath:certificates/test/digipost_test_root_ca.pem"
        )),
        PRODUCTION(asList(
               "classpath:certificates/prod/BPClass3CA3.cer",
               "classpath:certificates/prod/BPClass3RootCA.cer",
               "classpath:certificates/prod/commfides_ca.cer",
               "classpath:certificates/prod/commfides_root_ca.cer"
        ));

        private final List<String> certificatePaths;

        Certificates(List<String> certificatePaths) {
            this.certificatePaths = certificatePaths;
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
