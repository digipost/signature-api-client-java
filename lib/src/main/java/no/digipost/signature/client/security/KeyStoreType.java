package no.digipost.signature.client.security;

import no.digipost.signature.client.core.exceptions.KeyException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

enum KeyStoreType {
    PKCS12, JCEKS;

    KeyStore getKeyStoreInstance() {
        try {
            return KeyStore.getInstance(this.name());
        } catch (KeyStoreException e) {
            throw new KeyException(
                    "Unable to get key store instance of type " + this +
                            ", because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
    }

    KeyStore loadKeyStore(InputStream keyStoreStream, String keyStorePassword) {
        if (keyStoreStream == null) {
            throw new KeyException("Failed to initialize key store, because the key store stream is null. Please specify a stream with data.");
        }

        KeyStore ks = getKeyStoreInstance();
        try {
            ks.load(keyStoreStream, keyStorePassword.toCharArray());
        } catch (IOException | NoSuchAlgorithmException | java.security.cert.CertificateException e) {
            throw new KeyException(
                    "Unable to load key store instance of type " + this +
                            ", because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
        return ks;
    }
}
