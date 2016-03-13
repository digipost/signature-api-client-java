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
package no.digipost.signature.client.security;

import no.digipost.signature.client.core.exceptions.CertificateException;
import no.digipost.signature.client.core.exceptions.KeyException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class KeyStoreConfig {

    public final KeyStore keyStore;
    public final String alias;
    public final String keystorePassword;
    public final String privatekeyPassword;

    public KeyStoreConfig(final KeyStore keyStore, final String alias, final String keystorePassword, final String privatekeyPassword) {
        this.keyStore = keyStore;
        this.alias = alias;
        this.keystorePassword = keystorePassword;
        this.privatekeyPassword = privatekeyPassword;
    }

    public Certificate[] getCertificateChain() {
        try {
            return keyStore.getCertificateChain(alias);
        } catch (KeyStoreException e) {
            throw new KeyException("Failed to retrieve certificate chain from key store. Is key store initialized?", e);
        }
    }

    public X509Certificate getCertificate() {
        Certificate certificate;
        try {
            certificate = keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new CertificateException("Failed to retrieve certificate from key store. Is key store initialized?", e);
        }
        if (certificate == null) {
            throw new CertificateException("Failed to find certificate in key store. Are you sure a key store with a certificate is supplied and that you've given the right alias?");
        }

        if (!(certificate instanceof X509Certificate)) {
            throw new CertificateException("Only X509 certificates are supported. Got a certificate with type " + certificate.getClass().getSimpleName());
        }

        return (X509Certificate) certificate;
    }

    public PrivateKey getPrivateKey() {
        try {
            Key key = keyStore.getKey(alias, privatekeyPassword.toCharArray());
            if (!(key instanceof PrivateKey)) {
                throw new KeyException("Failed to retrieve private key from key store. Expected a PriveteKey, got " + key.getClass().getCanonicalName());
            }
            return (PrivateKey) key;
        } catch (KeyStoreException e) {
            throw new KeyException("Failed to retrieve private key from key store. Is key store initialized?", e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("Failed to retrieve private key from key store. Verify that the key is supported on the platform.", e);
        } catch (UnrecoverableKeyException e) {
            throw new KeyException("Failed to retrieve private key from key store. Verify that the password is correct.", e);
        }
    }

    public static KeyStoreConfig fromKeyStore(final InputStream keyStore, final String alias, final String keyStorePassword, final String privatekeyPassword) {
        try {
            KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(keyStore, keyStorePassword.toCharArray());
            return new KeyStoreConfig(ks, alias, keyStorePassword, privatekeyPassword);
        } catch (FileNotFoundException e) {
            throw new KeyException("Failed to initialize key store. Are you sure the file exists?", e);
        } catch (KeyStoreException | NoSuchAlgorithmException | IOException | java.security.cert.CertificateException e) {
            throw new KeyException("Failed to initialize key store", e);
        }
    }

}
