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
package no.digipost.signering.client.internal;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class SigneringHttpClient {

    // TODO (EHH): Initialisere denne "automatisk" fra crt-fil istedenfor å ha en keystore liggende i repoet (den inneholder bare public del, men kjedelig med et dummy passord her likevel)
    public static final KeyStoreConfig CLIENT_TRUSTSTORE =
            KeyStoreConfig.fraKeyStore(SigneringHttpClient.class.getResourceAsStream("/truststore.jks"), "root", "Qwer1234", null);

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
    /**
     * The connection request timeout for requests: {@value #CONNECTION_REQUEST_TIMEOUT_MS} ms.
     */
    public static final int CONNECTION_REQUEST_TIMEOUT_MS = 10_000;

    /**
     * Maximum <strong>{@value #MAX_CONNECTIONS_PER_ROUTE}</strong> connections
     * <em>per route</em>.
     * <p>
     * Apache HttpClient default: 2
     */
    public static final int MAX_CONNECTIONS_PER_ROUTE = 10;

    /**
     * Maximum <strong>{@value #MAX_CONNECTIONS_TOTAL}</strong>
     * total connections.
     * <p>
     * Apache HttpClient default: 20
     */
    public static final int MAX_CONNECTIONS_TOTAL = MAX_CONNECTIONS_PER_ROUTE;

    public static CloseableHttpClient create(KeyStoreConfig keyStoreConfig) {
        try {
            SSLConnectionSocketFactory sslsf = createSSLSocketFactory(keyStoreConfig);

            return HttpClientBuilder.create()
                    .setDefaultConnectionConfig(ConnectionConfig.DEFAULT)
                    .setDefaultSocketConfig(createSocketConfig(SOCKET_TIMEOUT_MS))
                    .setDefaultRequestConfig(createRequestConfig(CONNECTION_REQUEST_TIMEOUT_MS, CONNECT_TIMEOUT_MS, SOCKET_TIMEOUT_MS))
                    .setMaxConnTotal(MAX_CONNECTIONS_TOTAL)
                    .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                    .setSSLSocketFactory(sslsf)
                    .setConnectionManager(createConnectionManager(sslsf))
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestConfig createRequestConfig(final int connectionRequest, final int connect, final int socket) {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequest)
                .setConnectTimeout(connect)
                .setSocketTimeout(socket)
                .build();
    }

    public static SocketConfig createSocketConfig(int soTimeout) {
        return SocketConfig.custom().setSoTimeout(soTimeout).build();
    }

    private static SSLConnectionSocketFactory createSSLSocketFactory(final KeyStoreConfig keyStoreConfig) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStoreConfig.keyStore, keyStoreConfig.privatekeyPassword.toCharArray())
                .loadTrustMaterial(CLIENT_TRUSTSTORE.keyStore, new TrustSelfSignedStrategy()).build();
        return new SSLConnectionSocketFactory(sslcontext,
                new String[]{"TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
    }

    private static BasicHttpClientConnectionManager createConnectionManager(final SSLConnectionSocketFactory sslsf) {
        return new BasicHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build());
    }

}
