/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.NoSuchElementException;

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

        verifyCorrectAliasCasing(certificate);

        return (X509Certificate) certificate;
    }

    public PrivateKey getPrivateKey() {
        try {
            Key key = keyStore.getKey(alias, privatekeyPassword.toCharArray());
            if (!(key instanceof PrivateKey)) {
                throw new KeyException("Failed to retrieve private key from key store. Expected a PrivateKey, got " + key.getClass().getCanonicalName());
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

    /**
     * @deprecated as of 4.3, please use {@link #fromJavaKeyStore(InputStream, String, String, String)}(same functionality, different name), or load directly from organization
     * certificate using {{@link #fromOrganizationCertificate(InputStream, String)}}
     */
    @Deprecated
    public static KeyStoreConfig fromKeyStore(final InputStream javaKeyStore, final String alias, final String keyStorePassword, final String privatekeyPassword) {
        return fromJavaKeyStore(javaKeyStore, alias, keyStorePassword, privatekeyPassword);
    }

    /**
     * Create a {@link KeyStoreConfig} from a Java Key Store containing an Organization Certificate (Virksomhetssertifikat).
     *
     * @param javaKeyStore       A stream of the certificate in JCEKS format.
     * @param alias              The alias of the organization certificate in the key store.
     * @param keyStorePassword   The password for the key store itself.
     * @param privatekeyPassword The password for the private key of the organization certificate within the key store.
     * @return The config, containing the certificate, the private key and the certificate chain.
     */
    public static KeyStoreConfig fromJavaKeyStore(final InputStream javaKeyStore, final String alias, final String keyStorePassword, final String privatekeyPassword) {
        KeyStore ks = KeyStoreType.JCEKS.loadKeyStore(javaKeyStore, keyStorePassword);
        return new KeyStoreConfig(ks, alias, keyStorePassword, privatekeyPassword);

    }

    /**
     * Create a {@link KeyStoreConfig} from an Organization Certificate (Virksomhetssertifikat).
     *
     * @param organizationCertificateStream A stream of the certificate in PKCS12 format. The file should have .p12-file ending.
     * @param privatekeyPassword            The password for the private key of the organization certificate.
     * @return The config, containing the certificate, the private key and the certificate chain.
     */
    public static KeyStoreConfig fromOrganizationCertificate(final InputStream organizationCertificateStream, final String privatekeyPassword) {
        KeyStore ks = KeyStoreType.PKCS12.loadKeyStore(organizationCertificateStream, privatekeyPassword);
        try {
            Enumeration aliases = ks.aliases();
            String keyName = (String) aliases.nextElement();
            return new KeyStoreConfig(ks, keyName, privatekeyPassword, privatekeyPassword);
        } catch (KeyStoreException e) {
            throw new KeyException("Failed to initialize key store", e);
        } catch (NoSuchElementException e) {
            throw new KeyException("Could not find any aliases in the key store. Are you sure this is an organization certificate in PKCS12 format?");
        }
    }

    private void verifyCorrectAliasCasing(Certificate certificate) {
        try {
            String aliasFromKeystore = keyStore.getCertificateAlias(certificate);
            if (!aliasFromKeystore.equals(alias)) {
                throw new CertificateException("Certificate alias in keystore was not same as provided alias. Probably different casing. In keystore: " + aliasFromKeystore + ", from config: " + alias);
            }
        } catch (KeyStoreException e) {
            throw new CertificateException("Unable to get certificate alias based on certificate. This should never happen, as we just read the certificate from the same keystore.", e);
        }
    }

}
