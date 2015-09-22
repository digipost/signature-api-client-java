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

import no.posten.dpost.httpclient.DigipostHttpClientFactory;
import no.posten.dpost.httpclient.DigipostHttpClientSettings;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SigneringHttpClient {

    // TODO (EHH): Initialisere denne "automatisk" fra crt-fil istedenfor å ha en keystore liggende i repoet (den inneholder bare public del, men kjedelig med et dummy passord her likevel)
    public static final CertStoreConfig CLIENT_TRUSTSTORE = new CertStoreConfig("src/main/resources/truststore.jks", "Qwer1234", null);

    public static CloseableHttpClient create(CertStoreConfig keystoreConfig) {
        try {
            SSLContext sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(loadKeyStore(keystoreConfig), keystoreConfig.privatekeyPassword.toCharArray())
                    .loadTrustMaterial(loadKeyStore(CLIENT_TRUSTSTORE), new TrustSelfSignedStrategy()).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
                    new String[]{"TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);

            BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build());
            return DigipostHttpClientFactory
                    .createBuilder(DigipostHttpClientSettings.DEFAULT)
                    .setSSLSocketFactory(sslsf)
                    .setConnectionManager(connectionManager)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyStore loadKeyStore(CertStoreConfig certStoreConfig) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(new File(certStoreConfig.path)), certStoreConfig.keystorePassword.toCharArray());
            return keyStore;
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

}
