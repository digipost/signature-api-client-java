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
package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.core.exceptions.KeyException;
import no.digipost.signature.client.core.internal.security.TrustStoreLoader;
import no.digipost.signature.client.core.internal.xml.JaxbMessageReaderWriterProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.USER_AGENT;

public class SignatureHttpClientFactory {


    public static SignatureHttpClient create(ClientConfiguration config) {
        try {
            SSLContext sslcontext = createSSLContext(config);

            Client jerseyClient = JerseyClientBuilder.newBuilder()
                    .withConfig(createClientConfig(config))
                    .sslContext(sslcontext)
                    .hostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .register(new AddRequestHeaderFilter(USER_AGENT, config.getUserAgent()))
                    .build();
            return new DefaultClient(jerseyClient, config.getSignatureServiceRoot());
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            if (e instanceof UnrecoverableKeyException && "Given final block not properly padded".equals(e.getMessage())) {
                throw new KeyException("Unable to load key from keystore. Possible causes:\n" +
                        "* Wrong password for private key (the password for the keystore and the private key may not be the same)\n" +
                        "* Multiple private keys in the keystore with different passwords (private keys in the same key store must have the same password)", e);
            } else {
                throw new KeyException("Unable to load key from keystore", e);
            }
        }
    }

    private static SSLContext createSSLContext(final ClientConfiguration config) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        return SSLContexts.custom()
                .loadKeyMaterial(config.getKeyStoreConfig().keyStore, config.getKeyStoreConfig().privatekeyPassword.toCharArray(), new PrivateKeyStrategy() {
                    @Override
                    public String chooseAlias(Map<String, PrivateKeyDetails> aliases, Socket socket) {
                        return config.getKeyStoreConfig().alias;
                    }
                })
                .loadTrustMaterial(TrustStoreLoader.build(config), new PostenEnterpriseCertificateStrategy())
                .build();
    }

    private static ClientConfig createClientConfig(ClientConfiguration config) {
        ClientConfig jerseyConfig = new ClientConfig();
        jerseyConfig.property(ClientProperties.CONNECT_TIMEOUT, config.getConnectTimeoutMillis());
        jerseyConfig.property(ClientProperties.READ_TIMEOUT, config.getSocketTimeoutMillis());
        jerseyConfig.register(MultiPartFeature.class);
        jerseyConfig.register(JaxbMessageReaderWriterProvider.class);
        return jerseyConfig;
    }

    private static final class DefaultClient implements SignatureHttpClient {

        private final Client jerseyClient;
        private final WebTarget signatureServiceRoot;

        DefaultClient(Client jerseyClient, URI root) {
            this.jerseyClient = jerseyClient;
            this.signatureServiceRoot = jerseyClient.target(root);
        }

        @Override
        public WebTarget target(String uri) {
            return jerseyClient.target(uri);
        }

        @Override
        public WebTarget signatureServiceRoot() {
            return signatureServiceRoot;
        }

    }

}