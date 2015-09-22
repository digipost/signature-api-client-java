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

import no.digipost.signering.client.domain.exceptions.NoekkelException;
import no.digipost.signering.client.domain.exceptions.SertifikatException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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
            throw new NoekkelException("Kunne ikke hente privat nøkkel fra KeyStore. Er KeyStore initialisiert?", e);
        }
    }

    public X509Certificate getSertifikat() {
        Certificate certificate;
        try {
            certificate = keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new SertifikatException("Klarte ikke lese sertifikat fra keystore", e);
        }
        if (certificate == null) {
            throw new SertifikatException("Kunne ikke finne sertifikat i keystore. Er du sikker på at det er brukt keystore med et sertifikat og at du har oppgitt riktig alias?");
        }

        if (!(certificate instanceof X509Certificate)) {
            throw new SertifikatException("Klienten støtter kun X509-sertifikater. Fikk sertifikat av typen " + certificate.getClass().getSimpleName());
        }

        return (X509Certificate) certificate;
    }

    public PrivateKey getPrivateKey() {
        try {
            Key key = keyStore.getKey(alias, privatekeyPassword.toCharArray());
            if (!(key instanceof PrivateKey)) {
                throw new NoekkelException("Kunne ikke hente privat nøkkel fra key store. Forventet å få en PrivateKey, fikk " + key.getClass().getCanonicalName());
            }
            return (PrivateKey) key;
        } catch (KeyStoreException e) {
            throw new NoekkelException("Kunne ikke hente privat nøkkel fra KeyStore. Er KeyStore initialisiert?", e);
        } catch (NoSuchAlgorithmException e) {
            throw new NoekkelException("Kunne ikke hente privat nøkkel fra KeyStore. Verifiser at nøkkelen er støttet på plattformen", e);
        } catch (UnrecoverableKeyException e) {
            throw new NoekkelException("Kunne ikke hente privat nøkkel fra KeyStore. Sjekk at passordet er riktig.", e);
        }
    }

    public static KeyStoreConfig fraKeyStore(final String path, final String alias, final String keyStorePassword, final String privatekeyPassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(path), keyStorePassword.toCharArray());
            return new KeyStoreConfig(keyStore, alias, keyStorePassword, privatekeyPassword);
        } catch (FileNotFoundException e) {
            throw new NoekkelException("Kunne initialisiere KeyStore. Er du sikker på at filen finnes?", e);
        } catch (KeyStoreException | NoSuchAlgorithmException | IOException | CertificateException e) {
            throw new NoekkelException("Kunne initialisiere KeyStore", e);
        }
    }

}
