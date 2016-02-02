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
package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.ClientConfiguration;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContexts;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class SignatureHttpClient {

    /**
     * Socket timeout is used for both requests and, if any,
     * underlying layered sockets (typically for
     * secure sockets): {@value #SOCKET_TIMEOUT_MS} ms.
     */
    public static final int SOCKET_TIMEOUT_MS = 10_000;
    /**
     * The connect timeout for requests: {@value #CONNECT_TIMEOUT_MS} ms.
     */
    public static final int CONNECT_TIMEOUT_MS = 10_000;

    public static Client create(ClientConfiguration keyStoreConfig) {
        try {
            SSLContext sslcontext = createSSLContext(keyStoreConfig);

            return JerseyClientBuilder.newBuilder()
                    .withConfig(createClientConfig())
                    .sslContext(sslcontext)
                    .hostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static SSLContext createSSLContext(final ClientConfiguration config) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        return SSLContexts.custom()
                .loadKeyMaterial(config.getKeyStoreConfig().keyStore, config.getKeyStoreConfig().privatekeyPassword.toCharArray())
                .loadTrustMaterial(TrustStoreLoader.build(config), new PostenEnterpriseCertificateStrategy())
                .build();
    }

    private static ClientConfig createClientConfig() {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT_MS);
        config.property(ClientProperties.READ_TIMEOUT, SOCKET_TIMEOUT_MS);
        config.register(MultiPartFeature.class);
        config.register(JaxbMessageReaderWriterProvider.class);
        return config;
    }

}
