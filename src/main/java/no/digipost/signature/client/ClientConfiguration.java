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
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ClientConfiguration {

    private URI signatureServiceRoot;
    private KeyStoreConfig keyStoreConfig;
    private Sender sender;
    private List<String> certificatePaths = Certificates.PRODUCTION.certificatePaths;

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);

    private ClientConfiguration(URI signatureServiceRoot, KeyStoreConfig keyStoreConfig, Sender sender) {
        this.signatureServiceRoot = signatureServiceRoot;
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

    public static Builder builder(URI uri, KeyStoreConfig keystore, Sender sender) {
        return new Builder(uri, keystore, sender);
    }

    public static class Builder {

        private final ClientConfiguration target;

        private Builder(URI signatureServiceRoot, KeyStoreConfig keyStoreConfig, Sender sender) {
            this.target = new ClientConfiguration(signatureServiceRoot, keyStoreConfig, sender);
        }

        public Builder trustStore(Certificates... certificates) {
            if (asList(certificates).contains(Certificates.TEST)) {
                log.warn("Using test certificates in trust store. This should never be done for production environments.");
            }

            List<String> certificatePaths = new ArrayList<>();
            for (Certificates certificate : certificates) {
                certificatePaths.addAll(certificate.certificatePaths);
            }
            this.target.certificatePaths = certificatePaths;
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
}
